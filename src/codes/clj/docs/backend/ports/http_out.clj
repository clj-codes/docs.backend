(ns codes.clj.docs.backend.ports.http-out
  (:require [codes.clj.docs.backend.adapters :as adapters.price]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [parenthesin.components.http.clj-http :as components.http]))

;(defn get-btc-usd-price
  ;{:malli/schema [:=> [:cat schemas.types/HttpComponent] number?]}
  ;[http]
  ;(->> {:url "https://api.coindesk.com/v1/bpi/currentprice.json"
        ;:as :json
        ;:method :get}
       ;(components.http/request http)
       ;:body
       ;adapters.price/wire->usd-price))
