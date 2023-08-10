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

(defn upsert-author
  [login source database]
  (flow "insert data in the database and return"
    (state/invoke
     #(db/upsert-author {:author/login login
                         :author/account-source source
                         :author/avatar-url "https://my.pic.com/me.jpg"}
                        database))))

(defn create-example
  [example]
  (flow "insert new example"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/insert-example example)
         state-flow.api/return)))

(defn update-example
  [example]
  (flow "update example"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/update-example example)
         state-flow.api/return)))

(defflow author-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)]

  (upsert-author "delboni" "github" database)

  (flow "upsert author with new url"
    (state/invoke
     #(db/upsert-author {:author/login "delboni"
                         :author/account-source "github"
                         :author/avatar-url "https://my.pic.com/me2.jpg"}
                        database)))

  (flow "check transaction was inserted in db"
    (match? {:author/author-id uuid?
             :author/login "delboni"
             :author/account-source "github"
             :author/avatar-url "https://my.pic.com/me2.jpg"
             :author/created-at inst?}
            (db/get-author "delboni" :github database))))

(defflow see-also-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)
   author (upsert-author "delboni" "github" database)
   :let [author-id (:author/author-id author)]]

  (state/invoke
   #(db/insert-see-also {:see-also/author-id author-id
                         :see-also/definition-id "clojure.core/disj"
                         :see-also/definition-id-to "clojure.core/dissoc"}
                        database))

  (flow "check transaction was inserted in db"
    (match? [{:see-also/see-also-id uuid?
              :see-also/author author
              :see-also/definition-id "clojure.core/disj"
              :see-also/definition-id-to "clojure.core/dissoc"
              :see-also/created-at inst?}]
            (db/get-see-alsos "clojure.core/disj" database))))

(defflow note-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)
   author (upsert-author "delboni" "github" database)
   :let [author-id (:author/author-id author)]
   note (state/invoke
         #(db/insert-note {:note/author-id author-id
                           :note/definition-id "clojure.core/disj"
                           :note/body "my note about this function."}
                          database))]

  (flow "check transaction was inserted in db"
    (match? [{:note/note-id uuid?
              :note/author author
              :note/definition-id "clojure.core/disj"
              :note/body "my note about this function."
              :note/created-at inst?}]
            (db/get-notes "clojure.core/disj" database)))

  (state/invoke
   #(db/update-note {:note/note-id (:note/note-id note)
                     :note/author-id author-id
                     :note/definition-id "clojure.core/disj"
                     :note/body "edited my note about this function."}
                    database))

  (flow "check transaction was updated in db"
    (match? [{:note/note-id uuid?
              :note/author author
              :note/definition-id "clojure.core/disj"
              :note/body "edited my note about this function."
              :note/created-at inst?
              :note/updated-at inst?}]
            (db/get-notes "clojure.core/disj" database))))

(defflow example-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)
   author (upsert-author "delboni" "github" database)
   :let [author-id (:author/author-id author)]
   example-1 (create-example {:example/author-id author-id
                              :example/definition-id "clojure.core/disj"
                              :example/body "my example about this function."})
   example-2 (create-example {:example/author-id author-id
                              :example/definition-id "clojure.core/disj"
                              :example/body "another example about this function."})
   :let [example-full-1 (-> example-1
                            (assoc :example/author author)
                            (dissoc :example/author-id))
         example-full-2 (-> example-2
                            (assoc :example/author author)
                            (dissoc :example/author-id))]]

  (flow "check transaction was inserted in db"
    (match? [example-full-1
             example-full-2]
            (db/get-examples "clojure.core/disj" database)))

  (update-example {:example/example-id (:example/example-id example-1)
                   :example/author-id author-id
                   :example/body "my example about this function. edit 1"})

  (update-example {:example/example-id (:example/example-id example-1)
                   :example/author-id author-id
                   :example/body "my example about this function. edit 2"})

  (flow "check transaction was inserted in db"
    (match? [(assoc example-full-1 :example/body "my example about this function. edit 2")
             example-full-2]
            (db/get-examples "clojure.core/disj" database))))
