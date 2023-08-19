(ns integration.codes.clj.docs.backend.db.postgres-test
  (:require [clojure.test :refer [use-fixtures]]
            [codes.clj.docs.backend.db.postgres :as db]
            [com.stuartsierra.component :as component]
            [integration.codes.clj.docs.backend.util :as util]
            [integration.codes.clj.docs.backend.util.db.postgres :as util.db.postgres]
            [matcher-combinators.matchers :as m]
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

(defflow author-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)]

  (util.db.postgres/upsert-author "delboni" "github")

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
   author (util.db.postgres/upsert-author "delboni" "github")
   :let [author-id (:author/author-id author)]]

  (util.db.postgres/create-see-also {:see-also/author-id author-id
                                     :see-also/definition-id "clojure.core/disj"
                                     :see-also/definition-id-to "clojure.core/dissoc"})

  (flow "check transaction was inserted in db"
    (match? [{:definition/see-alsos [{:see-also/see-also-id uuid?
                                      :see-also/author author
                                      :see-also/definition-id "clojure.core/disj"
                                      :see-also/definition-id-to "clojure.core/dissoc"
                                      :see-also/created-at inst?}]}]
            (db/get-by-definition "clojure.core/disj" database))))

(defflow note-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)
   author (util.db.postgres/upsert-author "delboni" "github")
   :let [author-id (:author/author-id author)]
   note (util.db.postgres/create-note {:note/author-id author-id
                                       :note/definition-id "clojure.core/disj"
                                       :note/body "my note about this function."})]

  (flow "check transaction was inserted in db"
    (match? [{:definition/notes [{:note/note-id uuid?
                                  :note/author author
                                  :note/definition-id "clojure.core/disj"
                                  :note/body "my note about this function."
                                  :note/created-at inst?}]}]
            (db/get-by-definition "clojure.core/disj" database)))

  (util.db.postgres/update-note {:note/note-id (:note/note-id note)
                                 :note/author-id author-id
                                 :note/definition-id "clojure.core/disj"
                                 :note/body "edited my note about this function."})

  (flow "check transaction was updated in db"
    (match? [{:definition/notes [{:note/note-id uuid?
                                  :note/author author
                                  :note/definition-id "clojure.core/disj"
                                  :note/body "edited my note about this function."
                                  :note/created-at inst?
                                  ;todo: :note/updated-at inst?
                                  }]}]
            (db/get-by-definition "clojure.core/disj" database))))

(defflow example-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)
   author (util.db.postgres/upsert-author "delboni" "github")
   :let [author-id (:author/author-id author)]
   example-1 (util.db.postgres/create-example {:example/author-id author-id
                                               :example/definition-id "clojure.core/disj"
                                               :example/body "my example about this function."})
   example-2 (util.db.postgres/create-example {:example/author-id author-id
                                               :example/definition-id "clojure.core/disj"
                                               :example/body "another example about this function."})
   :let [example-full-1 (-> example-1
                            (assoc :example/author author)
                            (dissoc :example/author-id))
         example-full-2 (-> example-2
                            (assoc :example/author author)
                            (dissoc :example/author-id))]]

  (flow "check transaction was inserted in db"
    (match? [{:definition/examples [example-full-1
                                    example-full-2]}]
            (db/get-by-definition "clojure.core/disj" database)))

  (util.db.postgres/update-example {:example/example-id (:example/example-id example-1)
                                    :example/author-id author-id
                                    :example/body "my example about this function. edit 1"})

  (util.db.postgres/update-example {:example/example-id (:example/example-id example-1)
                                    :example/author-id author-id
                                    :example/body "my example about this function. edit 2"})

  (flow "check transaction was inserted in db"
    (match? [{:definition/examples [(assoc example-full-1
                                           :example/body "my example about this function. edit 2"
                                           :example/created-at inst?)
                                    example-full-2]}]
            (db/get-by-definition "clojure.core/disj" database))))

(defflow all-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)
   author (util.db.postgres/upsert-author "delboni" "github")
   :let [author-id (:author/author-id author)]
   _note-1 (util.db.postgres/create-note {:note/author-id author-id
                                          :note/definition-id "clojure.core/disj"
                                          :note/body "my note about this function."})
   _note-2 (util.db.postgres/create-note {:note/author-id author-id
                                          :note/definition-id "clojure.core/disj"
                                          :note/body "my second note about this function."})
   _see-also (util.db.postgres/create-see-also {:see-also/author-id author-id
                                                :see-also/definition-id "clojure.core/disj"
                                                :see-also/definition-id-to "clojure.core/dissoc"})
   _example-1 (util.db.postgres/create-example {:example/author-id author-id
                                                :example/definition-id "clojure.core/disj"
                                                :example/body "my example about this function."})
   _example-2 (util.db.postgres/create-example {:example/author-id author-id
                                                :example/definition-id "clojure.core/disj"
                                                :example/body "another example about this function."})]

  (flow "check transaction was inserted in db"
    (match? (m/match-with
             [vector? m/in-any-order]
             [{:definition/definition-id "clojure.core/disj"
               :definition/notes [#:note{:note-id uuid?
                                         :definition-id "clojure.core/disj"
                                         :body "my note about this function."
                                         :created-at inst?
                                         :author #:author{:author-id uuid?
                                                          :login "delboni"
                                                          :account-source "github"
                                                          :avatar-url "https://my.pic.com/me.jpg"
                                                          :created-at inst?}}
                                  #:note{:note-id uuid?
                                         :definition-id "clojure.core/disj"
                                         :body "my second note about this function."
                                         :created-at inst?
                                         :author #:author{:author-id uuid?
                                                          :login "delboni"
                                                          :account-source "github"
                                                          :avatar-url "https://my.pic.com/me.jpg"
                                                          :created-at
                                                          inst?}}]
               :definition/examples [#:example{:example-id uuid?
                                               :definition-id "clojure.core/disj"
                                               :body "my example about this function."
                                               :created-at inst?
                                               :author #:author{:author-id uuid?
                                                                :login "delboni"
                                                                :account-source "github"
                                                                :avatar-url "https://my.pic.com/me.jpg"
                                                                :created-at inst?}
                                               :editors [#:author{:author-id uuid?
                                                                  :login "delboni"
                                                                  :account-source "github"
                                                                  :avatar-url "https://my.pic.com/me.jpg"
                                                                  :created-at inst?}]}
                                     #:example{:example-id uuid?
                                               :definition-id "clojure.core/disj"
                                               :body "another example about this function."
                                               :created-at inst?
                                               :author #:author{:author-id uuid?
                                                                :login "delboni"
                                                                :account-source "github"
                                                                :avatar-url "https://my.pic.com/me.jpg"
                                                                :created-at inst?}
                                               :editors [#:author{:author-id uuid?
                                                                  :login "delboni"
                                                                  :account-source "github"
                                                                  :avatar-url "https://my.pic.com/me.jpg"
                                                                  :created-at inst?}]}]
               :definition/see-alsos [#:see-also{:see-also-id uuid?
                                                 :definition-id "clojure.core/disj"
                                                 :definition-id-to "clojure.core/dissoc"
                                                 :created-at inst?
                                                 :author #:author{:author-id uuid?
                                                                  :login "delboni"
                                                                  :account-source "github"
                                                                  :avatar-url "https://my.pic.com/me.jpg"
                                                                  :created-at inst?}}]}])
            (db/get-by-definition "clojure.core/disj" database))))
