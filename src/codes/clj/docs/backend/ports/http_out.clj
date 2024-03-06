(ns codes.clj.docs.backend.ports.http-out
  (:require [codes.clj.docs.backend.schemas.types :as schemas.types]
            [parenthesin.components.http.clj-http :as components.http]))

(defn github-exchange-code->access-token
  {:malli/schema [:=> [:cat :string schemas.types/Components] :string]}
  [code {:keys [config http]}]
  (let [{:keys [id secret]} (-> config :config :github :client)]
    (->> {:url "https://github.com/login/oauth/access_token"
          :as :json
          :method :post
          :form-params {:code code
                        :client_id id
                        :client_secret secret}
          :content-type :json}
         (components.http/request http)
         :body
         :access_token)))

(defn github-get-user-info
  {:malli/schema [:=> [:cat :string schemas.types/Components] :any]}
  [access-token {:keys [http]}]
  (->> {:url "https://api.github.com/user"
        :method :get
        :content-type :json
        :headers {:authorization (str "Bearer: " access-token)}}
       (components.http/request http)
       :body))
