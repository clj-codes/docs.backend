(ns unit.codes.clj.docs.backend.interceptors-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [codes.clj.docs.backend.interceptors :as backend.interceptors]
            [codes.clj.docs.backend.ports.jwt :as ports.jwt]
            [com.stuartsierra.component :as component]
            [matcher-combinators.test :refer [match?]]
            [parenthesin.components.config.aero :as config.aero]
            [parenthesin.helpers.malli :as helpers.malli]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defn- create-and-start-system!
  [{:keys [config]}]
  (component/start-system
   (component/system-map :config config)))

(def interceptor-fn (-> (backend.interceptors/auth-validate-jwt-interceptor) :enter))

(defn build-ctx [system jwt-token-request]
  {:response {:status 200}
   :request {:components system
             :headers {"authorization" jwt-token-request}}})

(deftest verify-request-test
  (let [system (create-and-start-system!
                {:config (config.aero/new-config {:jwt {:secret "app-jwt-secret"}
                                                  :env :test})})
        config-component (:config system)
        author {:author-id #uuid "2f36093a-239e-41ad-a533-c0a1be4f4300",
                :login "delboni",
                :account-source "github",
                :avatar-url "https://my.profile.pic/me.png",
                :created-at #inst "1970-01-01T01:17:53.353-00:00"}
        valid-jwt (->> (ports.jwt/encrypt author config-component)
                       (str "Bearer: "))]

    (testing "interceptor should check jwt auth token"
      (is (match? {:response {:status 200}}
                  (interceptor-fn
                   (build-ctx system valid-jwt))))

      (is (match? {:response {:status 401}}
                  (interceptor-fn
                   (build-ctx system "invalid-jwt-token")))))))
