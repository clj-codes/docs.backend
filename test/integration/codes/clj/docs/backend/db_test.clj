(ns integration.codes.clj.docs.backend.db-test
  (:require [clojure.test :refer [use-fixtures]]
            [codes.clj.docs.backend.db :as db]
            [com.stuartsierra.component :as component]
            [integration.codes.clj.docs.backend.util :as util]
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

(defn upsert-author
  [login source]
  (flow "insert data in the database and return"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/upsert-author {:author/login login
                            :author/account-source source
                            :author/avatar-url "https://my.pic.com/me.jpg"})
         state-flow.api/return)))

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

(defn create-note
  [note]
  (flow "insert new note"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/insert-note note)
         state-flow.api/return)))

(defn update-note
  [note]
  (flow "update note"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/update-note note)
         state-flow.api/return)))

(defn create-see-also
  [see-also]
  (flow "insert new see-also"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/insert-see-also see-also)
         state-flow.api/return)))

(defflow author-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)]

  (upsert-author "delboni" "github")

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
   author (upsert-author "delboni" "github")
   :let [author-id (:author/author-id author)]]

  (create-see-also {:see-also/author-id author-id
                    :see-also/definition-id "clojure.core/disj"
                    :see-also/definition-id-to "clojure.core/dissoc"})

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
   author (upsert-author "delboni" "github")
   :let [author-id (:author/author-id author)]
   note (create-note {:note/author-id author-id
                      :note/definition-id "clojure.core/disj"
                      :note/body "my note about this function."})]

  (flow "check transaction was inserted in db"
    (match? [{:note/note-id uuid?
              :note/author author
              :note/definition-id "clojure.core/disj"
              :note/body "my note about this function."
              :note/created-at inst?}]
            (db/get-notes "clojure.core/disj" database)))

  (update-note {:note/note-id (:note/note-id note)
                :note/author-id author-id
                :note/definition-id "clojure.core/disj"
                :note/body "edited my note about this function."})

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
   author (upsert-author "delboni" "github")
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

(defflow all-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [database (state-flow.api/get-state :database)
   author (upsert-author "delboni" "github")
   :let [author-id (:author/author-id author)]
   _note-1 (create-note {:note/author-id author-id
                         :note/definition-id "clojure.core/disj"
                         :note/body "my note about this function."})
   _note-2 (create-note {:note/author-id author-id
                         :note/definition-id "clojure.core/disj"
                         :note/body "my second note about this function."})
   _see-also (create-see-also {:see-also/author-id author-id
                               :see-also/definition-id "clojure.core/disj"
                               :see-also/definition-id-to "clojure.core/dissoc"})
   _example-1 (create-example {:example/author-id author-id
                               :example/definition-id "clojure.core/disj"
                               :example/body "my example about this function."})
   _example-2 (create-example {:example/author-id author-id
                               :example/definition-id "clojure.core/disj"
                               :example/body "another example about this function."})]

  (flow "check transaction was inserted in db"
    ;TODO adapt this into better schema
    (match? (m/in-any-order
             [{:account-source "github"
               :avatar-url "https://my.pic.com/me.jpg"
               :type "note"
               :login "delboni"
               :id uuid?
               :author-id uuid?
               :body "my note about this function."
               :created-at inst?
               :definition-id "clojure.core/disj"}
              {:account-source "github"
               :avatar-url "https://my.pic.com/me.jpg"
               :type "note"
               :login "delboni"
               :id uuid?
               :author-id uuid?
               :body "my second note about this function."
               :created-at inst?
               :definition-id "clojure.core/disj"}
              {:account-source "github"
               :avatar-url "https://my.pic.com/me.jpg"
               :type "example"
               :login "delboni"
               :id uuid?
               :author-id uuid?
               :body "my example about this function."
               :created-at inst?
               :definition-id "clojure.core/disj"}
              {:account-source "github"
               :avatar-url "https://my.pic.com/me.jpg"
               :type "example"
               :login "delboni"
               :id uuid?
               :author-id uuid?
               :body "another example about this function."
               :created-at inst?
               :definition-id "clojure.core/disj"}
              {:account-source "github"
               :avatar-url "https://my.pic.com/me.jpg"
               :type "see-also"
               :login "delboni"
               :id uuid?
               :author-id uuid?
               :body "clojure.core/dissoc"
               :created-at inst?
               :definition-id "clojure.core/disj"}])
            (db/get-all "clojure.core/disj" database))))

(comment
  (->> [{:account-source "github"
         :avatar-url "https://my.pic.com/me.jpg"
         :type "note"
         :login "delboni"
         :id 11
         :author-id uuid?
         :body "my note about this function."
         :created-at inst?
         :definition-id "clojure.core/disj"}
        {:account-source "github"
         :avatar-url "https://my.pic.com/me.jpg"
         :type "note"
         :login "delboni"
         :id 12
         :author-id uuid?
         :body "my second note about this function."
         :created-at 2
         :definition-id "clojure.core/disj"}
        {:account-source "github"
         :avatar-url "https://my.pic.com/me.jpg"
         :type "example"
         :login "delboni"
         :id 21
         :author-id uuid?
         :body "my example about this function."
         :created-at 1
         :definition-id "clojure.core/disj"}
        {:account-source "github"
         :avatar-url "https://my.pic.com/me.jpg"
         :type "example"
         :login "delboni"
         :id 21
         :author-id uuid?
         :body "my example about this function. edited"
         :created-at 2
         :definition-id "clojure.core/disj"}
        {:account-source "github"
         :avatar-url "https://my.pic.com/me.jpg"
         :type "example"
         :login "delboni"
         :id 21
         :author-id uuid?
         :body "my example about this function. edited again"
         :created-at 3
         :definition-id "clojure.core/disj"}
        {:account-source "github"
         :avatar-url "https://my.pic.com/me.jpg"
         :type "example"
         :login "delboni"
         :id 22
         :author-id uuid?
         :body "my example about this function."
         :created-at 1
         :definition-id "clojure.core/disj"}
        {:account-source "github"
         :avatar-url "https://my.pic.com/me.jpg"
         :type "see-also"
         :login "delboni"
         :id 31
         :author-id uuid?
         :body "clojure.core/dissoc"
         :created-at inst?
         :definition-id "clojure.core/disj"}]
       (group-by :definition-id)
       (map (fn [[definition-id items]]
              (let [notes (filter #(= (:type %) "note") items)
                    examples (->> items
                                  (filter #(= (:type %) "example"))
                                  (group-by :id)
                                  (map (fn [[_ examples]]
                                         (let [edits (sort-by :created-at examples)
                                               latest (last edits)]
                                           (assoc latest :editors edits)))))
                    see-alsos (filter #(= (:type %) "see-also") items)]
                {:definition-id definition-id
                 :notes notes
                 :examples examples
                 :see-alsos see-alsos})))))

