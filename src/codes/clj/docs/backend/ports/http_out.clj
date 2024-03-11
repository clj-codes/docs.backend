(ns codes.clj.docs.backend.ports.http-out
  (:require [codes.clj.docs.backend.adapters.social :as adapters.social]
            [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [parenthesin.components.http.clj-http :as components.http]))

(defn github-exchange-code->access-token
  {:malli/schema [:=> [:cat :string schemas.types/Components] :string]}
  [code {:keys [config http]}]
  (let [{:keys [id secret]} (-> config :config :github :client)]
    (->> {:url "https://github.com/login/oauth/access_token"
          :method :post
          :query-params {:code code
                         :client_id id
                         :client_secret secret}
          :accept :json
          :as :json}
         (components.http/request http)
         :body
         :access_token)))

(defn github-get-user-info
  {:malli/schema [:=> [:cat :string schemas.types/Components]
                  schemas.model.social/NewAuthor]}
  [access-token {:keys [http]}]
  (->> {:url "https://api.github.com/user"
        :method :get
        :content-type :json
        :headers {"Authorization" (str "Bearer " access-token)}
        :accept :json
        :as :json}
       (components.http/request http)
       :body
       adapters.social/github-user-wire->model))
