(ns codes.clj.docs.backend.db
  (:require [codes.clj.docs.backend.schemas.db :as schemas.db]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [honey.sql :as sql]
            [honey.sql.helpers :as sql.helpers]
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

(defn insert-author
  {:malli/schema [:=> [:cat schemas.db/NewAuthor schemas.types/DatabaseComponent] :any]}
  [transaction db]
  (->> (-> (sql.helpers/insert-into :authors)
           (sql.helpers/values [transaction])
           (sql.helpers/returning :*)
           sql/format)
       (components.database/execute db)
       first))

(defn get-author
  {:malli/schema [:=> [:cat :string :keyword schemas.types/DatabaseComponent] schemas.db/Author]}
  [login source db]
  (->> (-> (sql.helpers/select :*)
           (sql.helpers/from :authors)
           (sql.helpers/where :and
                              [:= :login login]
                              [:= :account_source (name source)])
           sql/format)
       (components.database/execute db)
       first))
