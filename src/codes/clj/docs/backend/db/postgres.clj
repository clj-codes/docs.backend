(ns codes.clj.docs.backend.db.postgres
  (:require [codes.clj.docs.backend.adapters.db.postgres :as adapters]
            [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [honey.sql :as sql]
            [honey.sql.helpers :as sql.helpers]
            [next.jdbc :as jdbc]
            [parenthesin.components.db.jdbc-hikari :as components.database]
            [taoensso.encore :as enc]))

(defn ^:private execute!
  {:malli/schema [:=> [:cat schemas.types/DatabaseComponent :any] :any]}
  [db sql-params]
  (components.database/execute db sql-params jdbc/unqualified-snake-kebab-opts))

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

(defn delete-see-also
  {:malli/schema [:=> [:cat :uuid schemas.types/DatabaseComponent]
                  schemas.model.social/SeeAlso]}
  [see-also-id db]
  (->> (-> (sql.helpers/delete-from :see-also)
           (sql.helpers/where [:= :see-also-id see-also-id])
           (sql.helpers/returning [:see-also-id :id]
                                  :definition-id
                                  [:definition-id-to :body]
                                  [:created-at :created])
           sql/format)
       (execute! db)
       first
       adapters/db->see-also))

(def get-see-also-query
  (-> (sql.helpers/select
       [:see-also/see-also-id :id]
       ["see-also" :type]
       :see-also/definition-id
       [:see-also/definition-id-to :body]
       [:see-also/created-at :created]
       :author-see-also/*)
      (sql.helpers/from :see-also)
      (sql.helpers/join [:author :author-see-also]
                        [:= :see-also/author-id :author-see-also/author-id])))

(defn get-see-also
  {:malli/schema [:=> [:cat :uuid schemas.types/DatabaseComponent]
                  [:maybe schemas.model.social/SeeAlso]]}
  [see-also-id db]
  (->> (-> get-see-also-query
           (sql.helpers/where [:= :see-also/see-also-id see-also-id])
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

(def get-example-query
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
                        [:= :example-edit/author-id :author-example-edit/author-id])))

(defn get-example
  {:malli/schema [:=> [:cat :uuid schemas.types/DatabaseComponent]
                  [:maybe schemas.model.social/Example]]}
  [example-id db]
  (->> (-> get-example-query
           (sql.helpers/where [:= :example/example-id example-id])
           sql/format)
       (execute! db)
       adapters/db->examples
       first))

(defn update-example
  {:malli/schema [:=> [:cat schemas.model.social/UpdateExample schemas.types/DatabaseComponent]
                  schemas.model.social/Example]}
  [transaction db]
  (let [edited-example (->> (-> (sql.helpers/insert-into :example-edit)
                                (sql.helpers/values [transaction])
                                (sql.helpers/returning :*)
                                sql/format)
                            (execute! db)
                            first)]
    (get-example (:example-id edited-example) db)))

(defn delete-example
  {:malli/schema [:=> [:cat :uuid :uuid schemas.types/DatabaseComponent]
                  schemas.model.social/Example]}
  [example-id author-id db]
  (let [query (-> (sql.helpers/with
                   [:example-deleted (-> (sql.helpers/delete-from :example-edit)
                                         (sql.helpers/where :in [:composite :example-id :author-id :created-at]
                                                            (-> (sql.helpers/select :example-id
                                                                                    :author-id
                                                                                    :created-at)
                                                                (sql.helpers/from :example-edit)
                                                                (sql.helpers/where :and
                                                                                   [:= :example-id example-id]
                                                                                   [:= :author-id author-id])
                                                                (sql.helpers/order-by [:created-at :desc])
                                                                (sql.helpers/limit 1)))
                                         (sql.helpers/returning :*))])
                  (sql.helpers/select [:example/example-id :id]
                                      :definition-id
                                      :body
                                      [:example-deleted/created-at :created])
                  (sql.helpers/from :example-deleted)
                  (sql.helpers/join :example
                                    [:= :example/example-id :example-deleted/example-id])
                  sql/format)
        example-before-delete (-> (execute! db query)
                                  first
                                  (adapters/db->example []))
        example-after-delete (get-example example-id db)]
    (when-not example-after-delete
      (execute! db (-> (sql.helpers/delete-from :example)
                       (sql.helpers/where [:= :example-id example-id])
                       sql/format)))
    (or example-after-delete
        example-before-delete)))

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

(defn delete-note
  {:malli/schema [:=> [:cat :uuid schemas.types/DatabaseComponent]
                  schemas.model.social/Note]}
  [note-id db]
  (->> (-> (sql.helpers/delete-from :note)
           (sql.helpers/where [:= :note-id note-id])
           (sql.helpers/returning [:note-id :id]
                                  :definition-id
                                  :body
                                  [:created-at :created])
           sql/format)
       (execute! db)
       first
       adapters/db->note))

(def get-note-query
  (-> (sql.helpers/select
       [:note/note-id :id]
       ["note" :type]
       :note/definition-id
       :note/body
       [:note/created-at :created]
       :author-note/*)
      (sql.helpers/from :note)
      (sql.helpers/join [:author :author-note]
                        [:= :note/author-id :author-note/author-id])))

(defn get-note
  {:malli/schema [:=> [:cat :uuid schemas.types/DatabaseComponent]
                  [:maybe schemas.model.social/Note]]}
  [note-id db]
  (->> (-> get-note-query
           (sql.helpers/where [:= :note/note-id note-id])
           sql/format)
       (execute! db)
       first
       adapters/db->note))

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

(defn get-author+socials
  {:malli/schema [:=> [:cat :string schemas.model.social/account-source schemas.types/DatabaseComponent]
                  [:maybe schemas.model.social/Author+Socials]]}
  [login source db]
  (when-let [author (->> (-> (sql.helpers/select :*)
                             (sql.helpers/from :author)
                             (sql.helpers/where :and
                                                [:= :login login]
                                                [:= :account_source source])
                             sql/format)
                         (execute! db)
                         first)]
    (let [author-id (:author-id author)
          socials (->> (-> (sql.helpers/union-all
                            (-> get-note-query
                                (sql.helpers/where [:= :note/author-id author-id]))

                            (-> get-example-query
                                (sql.helpers/where [:= :example-edit/author-id author-id]))

                            (-> get-see-also-query
                                (sql.helpers/where [:= :see-also/author-id author-id])))
                           sql/format)
                       (execute! db)
                       adapters/db->socials
                       seq)]

      (enc/assoc-some
       (adapters/db->author author)
       :author/socials socials))))

(defn get-by-definition
  {:malli/schema [:=> [:cat :string schemas.types/DatabaseComponent]
                  [:maybe schemas.model.social/Social]]}
  [definition-id db]
  (->> (-> (sql.helpers/union-all
            (-> get-note-query
                (sql.helpers/where [:= :note/definition-id definition-id]))

            (-> get-example-query
                (sql.helpers/where [:= :example/definition-id definition-id]))

            (-> get-see-also-query
                (sql.helpers/where [:= :see-also/definition-id definition-id])))
           sql/format)
       (execute! db)
       adapters/db->social-definition))

(defn get-top-authors
  {:malli/schema [:=> [:cat :int schemas.types/DatabaseComponent]
                  [:maybe [:sequential schemas.model.social/Author+Interactions]]]}
  [limit db]
  (->> (-> (sql.helpers/select
            :author/*
            [[:count :social/id] :interactions])
           (sql.helpers/from [(sql.helpers/union-all
                               (-> (sql.helpers/select
                                    [:note/note-id :id]
                                    [:note/author-id :author-id])
                                   (sql.helpers/from :note))
                               (-> (sql.helpers/select
                                    [:example-edit/example-edit-id :id]
                                    [:example-edit/author-id :author-id])
                                   (sql.helpers/from :example-edit))
                               (-> (sql.helpers/select
                                    [:see-also/see-also-id :id]
                                    [:see-also/author-id :author-id])
                                   (sql.helpers/from :see-also))) :social])
           (sql.helpers/join :author
                             [:= :social/author-id :author/author-id])
           (sql.helpers/group-by :author/author-id)
           (sql.helpers/order-by [:interactions :desc])
           (sql.helpers/limit limit)
           sql/format)
       (execute! db)
       adapters/db->author+interaction))

(defn get-latest-interactions
  {:malli/schema [:=> [:cat :int schemas.types/DatabaseComponent]
                  [:maybe [:sequential schemas.model.social/AnySocial]]]}
  [limit db]
  (->> (-> (sql.helpers/union-all
            get-note-query
            get-example-query
            get-see-also-query)
           (sql.helpers/order-by [:created :desc])
           (sql.helpers/limit limit)
           sql/format)
       (execute! db)
       adapters/db->any-socials))
