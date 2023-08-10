(ns codes.clj.docs.backend.db
  (:require [codes.clj.docs.backend.schemas.db :as schemas.db]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [honey.sql :as sql]
            [honey.sql.helpers :as sql.helpers]
            [next.jdbc :as jdbc]
            [parenthesin.components.db.jdbc-hikari :as components.database]))

(defn execute!
  {:malli/schema [:=> [:cat schemas.types/DatabaseComponent :any] :any]}
  [db sql-params]
  (components.database/execute db sql-params jdbc/snake-kebab-opts))

(defn upsert-author
  {:malli/schema [:=> [:cat schemas.db/NewAuthor schemas.types/DatabaseComponent] :any]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :author)
           (sql.helpers/values [transaction])
           (sql.helpers/upsert (-> (sql.helpers/on-conflict :login :account_source)
                                   (sql.helpers/do-update-set :avatar_url)))
           (sql.helpers/returning :*)
           sql/format)
       (execute! db)
       first))

(defn get-author
  {:malli/schema [:=> [:cat :string :keyword schemas.types/DatabaseComponent] schemas.db/Author]}
  [login source db]
  (->> (-> (sql.helpers/select :*)
           (sql.helpers/from :author)
           (sql.helpers/where :and
                              [:= :login login]
                              [:= :account_source (name source)])
           sql/format)
       (execute! db)
       first))

(defn insert-see-also
  {:malli/schema [:=> [:cat schemas.db/NewSeeAlso schemas.types/DatabaseComponent] :any]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :see-also)
           (sql.helpers/values [transaction])
           (sql.helpers/returning :*)
           sql/format)
       (execute! db)
       first))

(defn get-see-alsos
  {:malli/schema [:=> [:cat :string schemas.types/DatabaseComponent] [:sequential schemas.db/SeeAlso]]}
  [definition-id db]
  (->> (-> (sql.helpers/select :*)
           (sql.helpers/from :see-also)
           (sql.helpers/join :author
                             [:= :see-also/author-id :author/author-id])
           (sql.helpers/where [:= :definition-id definition-id])
           sql/format)
       (execute! db)
       (map (fn [{:author/keys [author-id login account-source avatar-url]
                  :see-also/keys [see-also-id definition-id definition-id-to created-at]
                  :as see-also}]
              #:see-also{:see-also-id see-also-id
                         :definition-id definition-id
                         :definition-id-to definition-id-to
                         :created-at created-at
                         :author {:author/author-id author-id
                                  :author/login login
                                  :author/account-source account-source
                                  :author/avatar-url avatar-url
                                  :author/created-at (:author/created-at see-also)}}))))

(defn insert-example
  {:malli/schema [:=> [:cat schemas.db/NewExample schemas.types/DatabaseComponent] :any]}
  [transaction db]
  (jdbc/with-transaction [datasource (:datasource db)]
    (let [execute-tx! (fn [db sql] (jdbc/execute! db sql jdbc/snake-kebab-opts))
          new-example (select-keys transaction [:example/definition-id])
          example-id (->> (-> (sql.helpers/insert-into :example)
                              (sql.helpers/values [new-example])
                              (sql.helpers/returning :example/example-id)
                              sql/format)
                          (execute-tx! datasource)
                          first
                          :example/example-id)
          example (-> transaction
                      (assoc :example/example-id example-id))]
      (->> (-> (sql.helpers/insert-into :example-edit)
               (sql.helpers/values [(dissoc example :example/definition-id)])
               sql/format)
           (execute-tx! datasource))
      example)))

(defn update-example
  {:malli/schema [:=> [:cat schemas.db/UpdateExample schemas.types/DatabaseComponent] :any]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :example-edit)
           (sql.helpers/values [transaction])
           (sql.helpers/returning :*)
           sql/format)
       (execute! db)
       first))

(defn get-examples
  {:malli/schema [:=> [:cat :string schemas.types/DatabaseComponent] [:sequential schemas.db/Example]]}
  [definition-id db]
  (->> (-> (sql.helpers/select-distinct-on [:example/created-at :example/example-id]
                                           :example/*
                                           :example-edit/*
                                           :author/*)
           (sql.helpers/from :example)
           (sql.helpers/join :example-edit
                             [:= :example/example-id :example-edit/example-id])
           (sql.helpers/join :author
                             [:= :example-edit/author-id :author/author-id])
           (sql.helpers/where [:= :example/definition-id definition-id])
           (sql.helpers/order-by [:example/created-at :asc])
           (sql.helpers/order-by [:example/example-id])
           (sql.helpers/order-by [:example-edit/created-at :desc])
           sql/format)
       (execute! db)
       (map (fn [{:author/keys [author-id login account-source avatar-url]
                  :example/keys [example-id definition-id]
                  :example-edit/keys [body created-at]
                  :as example}]
              #:example{:example-id example-id
                        :definition-id definition-id
                        :body body
                        :created-at created-at
                        :author {:author/author-id author-id
                                 :author/login login
                                 :author/account-source account-source
                                 :author/avatar-url avatar-url
                                 :author/created-at (:author/created-at example)}}))))

(defn insert-note
  {:malli/schema [:=> [:cat schemas.db/NewNote schemas.types/DatabaseComponent] :any]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :note)
           (sql.helpers/values [transaction])
           (sql.helpers/returning :*)
           sql/format)
       (execute! db)
       first))

(defn update-note
  {:malli/schema [:=> [:cat schemas.db/UpdateNote schemas.types/DatabaseComponent] :any]}
  [transaction db]
  (->> (-> (sql.helpers/update :note)
           (sql.helpers/set transaction)
           (sql.helpers/where [:= :note-id (:note/note-id transaction)])
           (sql.helpers/returning :*)
           sql/format)
       (execute! db)
       first))

(defn get-notes
  {:malli/schema [:=> [:cat :string schemas.types/DatabaseComponent] [:sequential schemas.db/Note]]}
  [definition-id db]
  (->> (-> (sql.helpers/select :*)
           (sql.helpers/from :note)
           (sql.helpers/join :author
                             [:= :note/author-id :author/author-id])
           (sql.helpers/where [:= :definition-id definition-id])
           sql/format)
       (execute! db)
       (map (fn [{:author/keys [author-id login account-source avatar-url]
                  :note/keys [note-id definition-id body created-at updated-at]
                  :as note}]
              #:note{:note-id note-id
                     :definition-id definition-id
                     :body body
                     :created-at created-at
                     :updated-at updated-at
                     :author {:author/author-id author-id
                              :author/login login
                              :author/account-source account-source
                              :author/avatar-url avatar-url
                              :author/created-at (:author/created-at note)}}))))
