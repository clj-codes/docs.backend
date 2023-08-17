(ns codes.clj.docs.backend.ports.http-in
  (:require [codes.clj.docs.backend.adapters.social :as adapters.social]
            [codes.clj.docs.backend.controllers.social :as controllers.social]))

;(defn get-history
  ;[{components :components}]
  ;(let [{:keys [entries usd-price]} (controllers/get-wallet components)]
    ;{:status 200
     ;:body (adapters/->wallet-history usd-price entries)}))

;(defn do-deposit!
  ;[{{{:keys [btc]} :body} :parameters
    ;components :components}]
  ;(if (pos? btc)
    ;{:status 201
     ;:body (-> btc
               ;(controllers/do-deposit! components)
               ;adapters/db->wire-in)}
    ;{:status 400
     ;:body "btc deposit amount can't be negative."}))

;(defn do-withdrawal!
  ;[{{{:keys [btc]} :body} :parameters
    ;components :components}]
  ;(if (neg? btc)
    ;(if-let [withdrawal (controllers/do-withdrawal! btc components)]
      ;{:status 201
       ;:body (adapters/db->wire-in withdrawal)}
      ;{:status 400
       ;:body "withdrawal amount bigger than the total in the wallet."})
    ;{:status 400
     ;:body "btc withdrawal amount can't be positive."}))

(defn upsert-author
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body {:id (-> author
                  adapters.social/upsert-author-wire->model
                  (controllers.social/upsert-author components)
                  :author/author-id)}})

(defn get-author
  [{{{:keys [login source]} :path} :parameters
    components :components}]
  (if-let [author (controllers.social/get-author login (keyword source) components)]
    {:status 200
     :body (adapters.social/author->model->wire author)}
    {:status 404
     :body "not found"}))
