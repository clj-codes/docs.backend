(ns codes.clj.docs.backend.controllers
  (:require [codes.clj.docs.backend.db :as db]
            [codes.clj.docs.backend.logics :as logics]
            [codes.clj.docs.backend.ports.http-out :as http-out]
            [codes.clj.docs.backend.schemas.db :as schemas.db]
            [codes.clj.docs.backend.schemas.types :as schemas.types]))

(defn- instant-now [] (java.util.Date/from (java.time.Instant/now)))

(def WalletHistory
  [:map
   [:entries [:vector schemas.db/WalletEntry]]
   [:usd-price pos?]])

(defn get-wallet
  {:malli/schema [:=> [:cat schemas.types/Components] WalletHistory]}
  [{:keys [http database]}]
  (let [current-usd-price (http-out/get-btc-usd-price http)
        wallet-entries (db/get-wallet-all-transactions database)]
    {:entries wallet-entries
     :usd-price current-usd-price}))

(defn do-deposit!
  {:malli/schema [:=> [:cat pos? schemas.types/Components] schemas.db/WalletEntry]}
  [btc {:keys [http database]}]
  (let [now (instant-now)
        current-usd-price (http-out/get-btc-usd-price http)
        entry (logics/->wallet-transaction now btc current-usd-price)]
    (db/insert-wallet-transaction entry database)))

(defn do-withdrawal!
  {:malli/schema [:=> [:cat neg? schemas.types/Components] [:maybe schemas.db/WalletEntry]]}
  [btc {:keys [http database]}]
  (when (logics/can-withdrawal? btc (db/get-wallet-total database))
    (let [now (instant-now)
          current-usd-price (http-out/get-btc-usd-price http)
          entry (logics/->wallet-transaction now btc current-usd-price)]
      (db/insert-wallet-transaction entry database))))
