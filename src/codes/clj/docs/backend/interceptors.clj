(ns codes.clj.docs.backend.interceptors
  (:require [clojure.string :as str]
            [codes.clj.docs.backend.ports.jwt :as ports.jwt]))

(defn auth-validate-jwt-interceptor []
  {:name ::auth-validate-jwt
   :enter (fn [context]
            (let [request (:request context)
                  {{:keys [config]} :components} request
                  token (-> (get-in context [:request :headers "authorization"])
                            str (str/split #":") last str/trim)]
              (try
                (assoc-in context
                          [:request :auth] (ports.jwt/decrypt token config))
                (catch Exception _ex
                  (assoc context :response {:headers {"Content-Type" "application/text"}
                                            :status 401
                                            :body "invalid request signature"})))))})
