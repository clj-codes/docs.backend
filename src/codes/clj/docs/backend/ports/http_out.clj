(ns codes.clj.docs.backend.ports.http-out)

;(defn get-btc-usd-price
  ;{:malli/schema [:=> [:cat schemas.types/HttpComponent] number?]}
  ;[http]
  ;(->> {:url "https://api.coindesk.com/v1/bpi/currentprice.json"
        ;:as :json
        ;:method :get}
       ;(components.http/request http)
       ;:body
       ;adapters.price/wire->usd-price))
