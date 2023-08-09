(ns codes.clj.docs.backend.db
  (:require [codes.clj.docs.backend.schemas.db :as schemas.db]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [honey.sql :as sql]
            [honey.sql.helpers :as sql.helpers]
            [next.jdbc :as jdbc]
            [parenthesin.components.db.jdbc-hikari :as components.database]))

;(defn insert-wallet-transaction
 ;{:malli/schema [:=> [:cat schemas.db/WalletTransaction schemas.types/DatabaseComponent] :any]}
  ;[transaction db]
  ;(->> (-> (sql.helpers/insert-into :wallet)
           ;(sql.helpers/values [transaction])
           ;(sql.helpers/returning :*)
           ;sql/format)
       ;(components.database/execute db)
       ;first))

;(defn get-wallet-all-transactions
  ;{:malli/schema [:=> [:cat schemas.types/DatabaseComponent] [:vector schemas.db/WalletEntry]]}
  ;[db]
  ;(components.database/execute
   ;db
   ;(-> (sql.helpers/select :id :btc_amount :usd_amount_at :created_at)
       ;(sql.helpers/from :wallet)
       ;sql/format)))

;(defn get-wallet-total
  ;{:malli/schema [:=> [:cat schemas.types/DatabaseComponent] number?]}
  ;[db]
  ;(->> (-> (sql.helpers/select :%sum.btc_amount)
           ;(sql.helpers/from :wallet)
           ;sql/format)
       ;(components.database/execute db)
       ;first
       ;:sum))

(defn execute!
  {:malli/schema [:=> [:cat schemas.types/DatabaseComponent :any] :any]}
  [db sql-params]
  (components.database/execute db sql-params jdbc/snake-kebab-opts))

(defn insert-author
  {:malli/schema [:=> [:cat schemas.db/NewAuthor schemas.types/DatabaseComponent] :any]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :author)
           (sql.helpers/values [transaction])
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
  {:malli/schema [:=> [:cat :string schemas.types/DatabaseComponent] [:vector schemas.db/SeeAlso]]}
  [definition-id db]
  (->> (-> (sql.helpers/select :*)
           (sql.helpers/from :see-also)
           (sql.helpers/where [:= :definition-id definition-id])
           sql/format)
       (execute! db)))

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
  {:malli/schema [:=> [:cat :string schemas.types/DatabaseComponent] [:vector schemas.db/Example]]}
  [definition-id db]
  (->> (-> (sql.helpers/select-distinct-on [:example/created-at :example/example-id]
                                           :example/*
                                           :example-edit/*)
           (sql.helpers/from :example)
           (sql.helpers/join :example-edit
                             [:= :example/example-id :example-edit/example-id])
           (sql.helpers/where [:= :example/definition-id definition-id])
           (sql.helpers/order-by [:example/created-at :asc])
           (sql.helpers/order-by [:example/example-id])
           (sql.helpers/order-by [:example-edit/created-at :desc])
           sql/format)
       (execute! db)
       (mapv (fn [{:example/keys [example-id definition-id]
                   :example-edit/keys [author-id body created-at]}]
               #:example{:example-id example-id
                         :definition-id definition-id
                         :author-id author-id
                         :body body
                         :created-at created-at}))))

(defn insert-note
  {:malli/schema [:=> [:cat schemas.db/NewNote schemas.types/DatabaseComponent] :any]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :note)
           (sql.helpers/values [transaction])
           (sql.helpers/returning :*)
           sql/format)
       (execute! db)
       first))

(defn get-notes
  {:malli/schema [:=> [:cat :string schemas.types/DatabaseComponent] [:vector schemas.db/Note]]}
  [definition-id db]
  (->> (-> (sql.helpers/select :*)
           (sql.helpers/from :note)
           (sql.helpers/where [:= :definition-id definition-id])
           sql/format)
       (execute! db)))

; todo update note
