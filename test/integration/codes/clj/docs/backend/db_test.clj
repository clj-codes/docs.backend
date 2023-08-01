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

(defn create-author
  [login source database]
  (flow "insert data in the database and return"
    (state/invoke
     #(db/insert-author {:author/login login
                         :author/account-source source
                         :author/avatar-url "https://my.pic.com/me.jpg"}
                        database))))

(defflow author-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)]

  (create-author "delboni" "github" database)

  (flow "check transaction was inserted in db"
    (match? {:author/author-id uuid?
             :author/login "delboni"
             :author/account-source "github"
             :author/avatar-url "https://my.pic.com/me.jpg"
             :author/created-at inst?}
            (db/get-author "delboni" :github database))))

(defflow see-also-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)
   author (create-author "delboni" "github" database)
   :let [author-id (:author/author-id author)]]

  (state/invoke
   #(db/insert-see-also {:see-also/author-id author-id
                         :see-also/definition-id "clojure.core/disj"}
                        database))

  (flow "check transaction was inserted in db"
    (match? [{:see-also/see-also-id uuid?
              :see-also/author-id author-id
              :see-also/definition-id "clojure.core/disj"
              :see-also/created-at inst?}]
            (db/get-see-alsos "clojure.core/disj" database))))

(defflow note-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)
   author (create-author "delboni" "github" database)
   :let [author-id (:author/author-id author)]]

  (state/invoke
   #(db/insert-note {:note/author-id author-id
                     :note/definition-id "clojure.core/disj"
                     :note/body "my note about this function."}
                    database))

  (flow "check transaction was inserted in db"
    (match? [{:note/note-id uuid?
              :note/author-id author-id
              :note/definition-id "clojure.core/disj"
              :note/body "my note about this function."
              :note/created-at inst?}]
            (db/get-notes "clojure.core/disj" database))))
