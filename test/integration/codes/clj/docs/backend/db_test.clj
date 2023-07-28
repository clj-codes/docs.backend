(ns integration.codes.clj.docs.backend.db-test
  (:require [clojure.test :refer [use-fixtures]]
            [codes.clj.docs.backend.db :as db]
            [com.stuartsierra.component :as component]
            [integration.codes.clj.docs.backend.util :as util]
            [parenthesin.components.config.aero :as components.config]
            [parenthesin.components.db.jdbc-hikari :as components.database]
            [parenthesin.helpers.malli :as helpers.malli]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]
            [state-flow.state :as state]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defn- create-and-start-components! []
  (component/start-system
   (component/system-map
    :config (components.config/new-config)
    :database (component/using (components.database/new-database)
                               [:config]))))

(defflow
  flow-integration-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "creates a table, insert data and checks return in the database"
    [database (state-flow.api/get-state :database)]

    (state/invoke
     #(db/insert-author {:author/login "delboni"
                         :author/account_source "github"
                         :author/avatar_url "https://my.pic.com/me.jpg"}
                        database))

    (flow "check transaction was inserted in db"
      (match? {:author/author_id uuid?
               :author/login "delboni"
               :author/account_source "github"
               :author/avatar_url "https://my.pic.com/me.jpg"
               :author/created_at inst?}
              (db/get-author "delboni" :github database)))))
