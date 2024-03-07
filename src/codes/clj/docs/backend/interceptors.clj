(ns codes.clj.docs.backend.interceptors
  (:require [codes.clj.docs.backend.ports.jwt :as ports.jwt]))

; TODO: use this
(defn auth-validate-jwt-interceptor []
  {:name ::auth-validate-jwt
   :enter (fn [ctx]
            (let [request (:request ctx)
                  {{:keys [config]} :components} request
                  token (get-in ctx [:request :headers "Authorization"])]
              (try
                (assoc ctx :author (ports.jwt/decrypt token config))
                (catch Exception _ex
                  (assoc ctx :response {:headers {"Content-Type" "application/text"}
                                        :status 401
                                        :body "invalid request signature"})))))})
