(ns integration.codes.clj.docs.backend.social-test
  (:require [clojure.test :refer [use-fixtures]]
            [codes.clj.docs.backend.routes :as routes]
            [com.stuartsierra.component :as component]
            [integration.codes.clj.docs.backend.util :as util]
            [matcher-combinators.matchers :as matchers]
            [parenthesin.components.config.aero :as components.config]
            [parenthesin.components.db.jdbc-hikari :as components.database]
            [parenthesin.components.http.clj-http :as components.http]
            [parenthesin.components.router.reitit-malli :as components.router]
            [parenthesin.components.server.reitit-pedestal-jetty :as components.webserver]
            [parenthesin.helpers.malli :as helpers.malli]
            [parenthesin.helpers.state-flow.server.pedestal :as state-flow.server]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defn- create-and-start-components! []
  (component/start-system
   (component/system-map
    :config (components.config/new-config)
    :http (components.http/new-http-mock {})
    :router (components.router/new-router routes/routes)
    :database (component/using (components.database/new-database)
                               [:config])
    :webserver (component/using (components.webserver/new-webserver)
                                [:config :http :router :database]))))

(defflow
  flow-integration-wallet-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "should create author"
      (match? (matchers/embeds {:status 201
                                :body  {:id string?}})
              (state-flow.server/request! {:method :post
                                           :uri    "/author/"
                                           :body   {:login "delboni"
                                                    :account-source "github"
                                                    :avatar-url "https://my.pic/me.jpg"}})))

    (flow "should return author"
      (match? (matchers/embeds {:status 200
                                :body {:author-id string?
                                       :login "delboni",
                                       :account-source "github",
                                       :avatar-url "https://my.pic/me.jpg",
                                       :created-at string?}})
              (state-flow.server/request! {:method :get
                                           :uri    "/author/delboni/github"})))))
