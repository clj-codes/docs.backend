(ns codes.clj.docs.backend.db.postgres
  (:require [codes.clj.docs.backend.adapters.db.postgres :as adapters]
            [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [honey.sql :as sql]
            [honey.sql.helpers :as sql.helpers]
            [next.jdbc :as jdbc]
            [parenthesin.components.db.jdbc-hikari :as components.database]))

(defn ^:private execute!
  {:malli/schema [:=> [:cat schemas.types/DatabaseComponent :any] :any]}
  [db sql-params]
  (components.database/execute db sql-params jdbc/unqualified-snake-kebab-opts))

(defn upsert-author
  {:malli/schema [:=> [:cat schemas.model.social/NewAuthor schemas.types/DatabaseComponent]
                  schemas.model.social/Author]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :author)
           (sql.helpers/values [transaction])
           (sql.helpers/upsert (-> (sql.helpers/on-conflict :login :account_source)
                                   (sql.helpers/do-update-set :avatar_url)))
           (sql.helpers/returning :*)
           sql/format)
       (execute! db)
       first
       adapters/db->author))

(defn get-author
  {:malli/schema [:=> [:cat :string schemas.model.social/account-source schemas.types/DatabaseComponent]
                  [:maybe schemas.model.social/Author]]}
  [login source db]
  (when-let [author (->> (-> (sql.helpers/select :*)
                             (sql.helpers/from :author)
                             (sql.helpers/where :and
                                                [:= :login login]
                                                [:= :account_source source])
                             sql/format)
                         (execute! db)
                         first)]
    (adapters/db->author author)))

(defn insert-see-also
  {:malli/schema [:=> [:cat schemas.model.social/NewSeeAlso schemas.types/DatabaseComponent]
                  schemas.model.social/SeeAlso]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :see-also)
           (sql.helpers/values [transaction])
           (sql.helpers/returning [:see-also-id :id]
                                  :definition-id
                                  [:definition-id-to :body]
                                  [:created-at :created])
           sql/format)
       (execute! db)
       first
       adapters/db->see-also))

(defn insert-example
  {:malli/schema [:=> [:cat schemas.model.social/NewExample schemas.types/DatabaseComponent]
                  schemas.model.social/Example]}
  [transaction db]
  (jdbc/with-transaction [datasource (:datasource db)]
    (let [execute-tx! (fn [db sql] (jdbc/execute! db sql jdbc/snake-kebab-opts))
          new-example (select-keys transaction [:example/definition-id])
          {:example/keys [example-id
                          created-at]} (->> (-> (sql.helpers/insert-into :example)
                                                (sql.helpers/values [new-example])
                                                (sql.helpers/returning :example-id
                                                                       :created-at)
                                                sql/format)
                                            (execute-tx! datasource)
                                            first)
          example (-> transaction
                      (assoc :example/example-id example-id
                             :example/created-at created-at))]
      (->> (-> (sql.helpers/insert-into :example-edit)
               (sql.helpers/values [(dissoc example :example/definition-id)])
               sql/format)
           (execute-tx! datasource))
      example)))

(defn update-example
  {:malli/schema [:=> [:cat schemas.model.social/UpdateExample schemas.types/DatabaseComponent]
                  schemas.model.social/Example]}
  [transaction db]
  (let [example (->> (->  (sql.helpers/with
                           [:example-edited (-> (sql.helpers/insert-into :example-edit)
                                                (sql.helpers/values [transaction])
                                                (sql.helpers/returning :*))])
                          (sql.helpers/select [:example/example-id :id]
                                              :definition-id
                                              :body
                                              [:example-edited/created-at :created])
                          (sql.helpers/from :example-edited)
                          (sql.helpers/join :example
                                            [:= :example/example-id :example-edited/example-id])
                          sql/format)
                     (execute! db)
                     first)]
    (adapters/db->example example [])))

(defn insert-note
  {:malli/schema [:=> [:cat schemas.model.social/NewNote schemas.types/DatabaseComponent]
                  schemas.model.social/Note]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :note)
           (sql.helpers/values [transaction])
           (sql.helpers/returning [:note-id :id]
                                  :definition-id
                                  :body
                                  [:created-at :created])
           sql/format)
       (execute! db)
       first
       adapters/db->note))

(defn update-note
  {:malli/schema [:=> [:cat schemas.model.social/UpdateNote schemas.types/DatabaseComponent]
                  schemas.model.social/Note]}
  [transaction db]
  (->> (-> (sql.helpers/update :note)
           (sql.helpers/set transaction)
           (sql.helpers/where [:= :note-id (:note/note-id transaction)])
           (sql.helpers/returning [:note-id :id]
                                  :definition-id
                                  :body
                                  [:created-at :created])
           sql/format)
       (execute! db)
       first
       adapters/db->note))

(defn get-by-definition
  {:malli/schema [:=> [:cat :string schemas.types/DatabaseComponent]
                  schemas.model.social/Social]}
  [definition-id db]
  (->> (-> (sql.helpers/union-all
            (-> (sql.helpers/select
                 [:note/note-id :id]
                 ["note" :type]
                 :note/definition-id
                 :note/body
                 [:note/created-at :created]
                 :author-note/*)
                (sql.helpers/from :note)
                (sql.helpers/join [:author :author-note]
                                  [:= :note/author-id :author-note/author-id])
                (sql.helpers/where [:= :note/definition-id definition-id]))

            (-> (sql.helpers/select
                 [:example/example-id :id]
                 ["example" :type]
                 :example/definition-id
                 :example-edit/body
                 [:example-edit/created-at :created]
                 :author-example-edit/*)
                (sql.helpers/from :example)
                (sql.helpers/join :example-edit
                                  [:= :example/example-id :example-edit/example-id])
                (sql.helpers/join [:author :author-example-edit]
                                  [:= :example-edit/author-id :author-example-edit/author-id])
                (sql.helpers/where [:= :example/definition-id definition-id]))

            (-> (sql.helpers/select
                 [:see-also/see-also-id :id]
                 ["see-also" :type]
                 :see-also/definition-id
                 [:see-also/definition-id-to :body]
                 [:see-also/created-at :created]
                 :author-see-also/*)
                (sql.helpers/from :see-also)
                (sql.helpers/join [:author :author-see-also]
                                  [:= :see-also/author-id :author-see-also/author-id])
                (sql.helpers/where [:= :see-also/definition-id definition-id])))
           sql/format)
       (execute! db)
       adapters/db->social))
