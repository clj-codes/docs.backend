(ns integration.codes.clj.docs.backend.db.postgres-test
  (:require [clojure.test :refer [use-fixtures]]
            [codes.clj.docs.backend.db.postgres :as db]
            [com.stuartsierra.component :as component]
            [integration.codes.clj.docs.backend.util :as util]
            [integration.codes.clj.docs.backend.util.db.postgres :as util.db.postgres]
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

  [database (state-flow.api/get-state :database)

  ; prepare db authors
   author-1 (util.db.postgres/upsert-author "delboni" "github")
   author-2 (util.db.postgres/upsert-author "not-delboni" "github")

  ; prepare socials
   see-also-1 (util.db.postgres/create-see-also {:see-also/author-id (:author/author-id author-1)
                                                 :see-also/definition-id "clojure.core/disj"
                                                 :see-also/definition-id-to "clojure.core/dissoc"})
   _see-also-2 (util.db.postgres/create-see-also {:see-also/author-id (:author/author-id author-2)
                                                  :see-also/definition-id "clojure.core/disj"
                                                  :see-also/definition-id-to "clojure.core/dissoc2"})
   note-1 (util.db.postgres/create-note {:note/author-id (:author/author-id author-1)
                                         :note/definition-id "clojure.core/disj"
                                         :note/body "author 1 note about this function."})
   _note-2 (util.db.postgres/create-note {:note/author-id (:author/author-id author-2)
                                          :note/definition-id "clojure.core/disj"
                                          :note/body "author 2 note about this function."})
   example-1 (util.db.postgres/create-example {:example/author-id (:author/author-id author-1)
                                               :example/definition-id "clojure.core/disj"
                                               :example/body "author 1 example about this function."})
   _example-2 (util.db.postgres/create-example {:example/author-id (:author/author-id author-2)
                                                :example/definition-id "clojure.core/disj"
                                                :example/body "author 2 example about this function."})]

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
             :author/created-at inst?
             :author/socials [{:social/definition-id "clojure.core/disj"
                               :social/notes [note-1]
                               :social/examples [(dissoc example-1
                                                         :example/author-id)]
                               :social/see-alsos [see-also-1]}]}

            (db/get-author "delboni" "github" database))))

(defflow see-also-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [author (util.db.postgres/upsert-author "delboni" "github")
   :let [author-id (:author/author-id author)]
   see-also (util.db.postgres/create-see-also {:see-also/author-id author-id
                                               :see-also/definition-id "clojure.core/disj"
                                               :see-also/definition-id-to "clojure.core/dissoc"})]

  (flow "check transaction was inserted in db"
    (match? {:social/see-alsos [{:see-also/see-also-id uuid?
                                 :see-also/author author
                                 :see-also/definition-id "clojure.core/disj"
                                 :see-also/definition-id-to "clojure.core/dissoc"
                                 :see-also/created-at inst?}]}
            (util.db.postgres/get-by-definition "clojure.core/disj")))

  (flow "check see-also using get-see-also fn"
    (match? {:see-also/see-also-id uuid?
             :see-also/author author
             :see-also/definition-id "clojure.core/disj"
             :see-also/definition-id-to "clojure.core/dissoc"
             :see-also/created-at inst?}
            (util.db.postgres/get-see-also (:see-also/see-also-id see-also))))

  (flow "delete see-also in db"
    (match? {:see-also/see-also-id (:see-also/see-also-id see-also)
             :see-also/definition-id "clojure.core/disj"
             :see-also/definition-id-to "clojure.core/dissoc"
             :see-also/created-at inst?}
            (util.db.postgres/delete-see-also (:see-also/see-also-id see-also))))

  (flow "check see-also using get-see-also fn"
    (match? nil
            (util.db.postgres/get-see-also (:see-also/see-also-id see-also)))))

(defflow note-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [author (util.db.postgres/upsert-author "delboni" "github")
   :let [author-id (:author/author-id author)]
   note (util.db.postgres/create-note {:note/author-id author-id
                                       :note/definition-id "clojure.core/disj"
                                       :note/body "my note about this function."})]

  (flow "check transaction was inserted in db"
    (match? {:social/notes [{:note/note-id uuid?
                             :note/author author
                             :note/definition-id "clojure.core/disj"
                             :note/body "my note about this function."
                             :note/created-at inst?}]}
            (util.db.postgres/get-by-definition "clojure.core/disj")))

  (util.db.postgres/update-note {:note/note-id (:note/note-id note)
                                 :note/author-id author-id
                                 :note/definition-id "clojure.core/disj"
                                 :note/body "edited my note about this function."})

  (flow "check transaction was updated in db"
    (match? {:social/notes [{:note/note-id uuid?
                             :note/author author
                             :note/definition-id "clojure.core/disj"
                             :note/body "edited my note about this function."
                             :note/created-at inst?}]}
            (util.db.postgres/get-by-definition "clojure.core/disj")))

  (flow "check note using get-note fn"
    (match? {:note/note-id uuid?
             :note/author author
             :note/definition-id "clojure.core/disj"
             :note/body "edited my note about this function."
             :note/created-at inst?}
            (util.db.postgres/get-note (:note/note-id note))))

  (flow "delete note in db"
    (match? {:note/note-id (:note/note-id note)
             :note/definition-id "clojure.core/disj"
             :note/body "edited my note about this function."
             :note/created-at inst?}
            (util.db.postgres/delete-note (:note/note-id note))))

  (flow "check note using get-note fn"
    (match? nil
            (util.db.postgres/get-note (:note/note-id note)))))

(defflow example-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [author (util.db.postgres/upsert-author "delboni" "github")
   author-2 (util.db.postgres/upsert-author "ricardorico" "github")
   :let [author-id (:author/author-id author)
         author-2-id (:author/author-id author-2)]
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
    (match? {:social/examples [example-full-1
                               example-full-2]}
            (util.db.postgres/get-by-definition "clojure.core/disj")))

  (flow "check transaction was updated in db part 1"
    (match? #:example{:example-id uuid?
                      :definition-id "clojure.core/disj"
                      :body "my example about this function. edit 1"
                      :created-at inst?
                      :author #:author{:author-id uuid?
                                       :login "delboni"
                                       :account-source "github"
                                       :avatar-url "https://my.pic.com/me.jpg"
                                       :created-at inst?}
                      :editors [{:author/author-id uuid?
                                 :author/login "delboni"
                                 :author/account-source "github"
                                 :author/avatar-url "https://my.pic.com/me.jpg"
                                 :author/created-at inst?
                                 :editor/edited-at inst?}
                                {:author/author-id uuid?
                                 :author/login "delboni"
                                 :author/account-source "github"
                                 :author/avatar-url "https://my.pic.com/me.jpg"
                                 :author/created-at inst?
                                 :editor/edited-at inst?}]}
            (util.db.postgres/update-example {:example/example-id (:example/example-id example-1)
                                              :example/author-id author-id
                                              :example/body "my example about this function. edit 1"})))

  [edited-example-full-1 (util.db.postgres/update-example {:example/example-id (:example/example-id example-1)
                                                           :example/author-id author-2-id
                                                           :example/body "my example about this function. edit 2"})]
  (flow "check transaction was updated in db part 2"
    (match? #:example{:example-id uuid?
                      :definition-id "clojure.core/disj"
                      :body "my example about this function. edit 2"
                      :created-at inst?
                      :author #:author{:author-id uuid?
                                       :login "ricardorico"
                                       :account-source "github"
                                       :avatar-url "https://my.pic.com/me.jpg"
                                       :created-at inst?}
                      :editors [{:author/author-id uuid?
                                 :author/login "delboni"
                                 :author/account-source "github"
                                 :author/avatar-url "https://my.pic.com/me.jpg"
                                 :author/created-at inst?
                                 :editor/edited-at inst?}
                                {:author/author-id uuid?
                                 :author/login "delboni"
                                 :author/account-source "github"
                                 :author/avatar-url "https://my.pic.com/me.jpg"
                                 :author/created-at inst?
                                 :editor/edited-at inst?}
                                {:author/author-id uuid?
                                 :author/login "ricardorico"
                                 :author/account-source "github"
                                 :author/avatar-url "https://my.pic.com/me.jpg"
                                 :author/created-at inst?
                                 :editor/edited-at inst?}]}
            edited-example-full-1))

  (flow "check transaction was inserted in db"
    (match? {:social/examples [edited-example-full-1
                               example-full-2]}
            (util.db.postgres/get-by-definition "clojure.core/disj")))

  (flow "check example using get-example fn"
    (match? example-full-2
            (util.db.postgres/get-example (:example/example-id example-2))))

  (flow "delete latest revision for current author part 1"
    (util.db.postgres/delete-example (:example/example-id example-1) author-2-id)

    (match? #:example{:example-id uuid?
                      :definition-id "clojure.core/disj"
                      :body "my example about this function. edit 1"
                      :created-at inst?
                      :author #:author{:author-id uuid?
                                       :login "delboni"
                                       :account-source "github"
                                       :avatar-url "https://my.pic.com/me.jpg"
                                       :created-at inst?}
                      :editors [{:author/author-id uuid?
                                 :author/login "delboni"
                                 :author/account-source "github"
                                 :author/avatar-url "https://my.pic.com/me.jpg"
                                 :author/created-at inst?
                                 :editor/edited-at inst?}
                                {:author/author-id uuid?
                                 :author/login "delboni"
                                 :author/account-source "github"
                                 :author/avatar-url "https://my.pic.com/me.jpg"
                                 :author/created-at inst?
                                 :editor/edited-at inst?}]}
            (util.db.postgres/get-example (:example/example-id example-1))))

  (flow "delete latest revision for current author part 2"
    (util.db.postgres/delete-example (:example/example-id example-1) author-id)

    (match? #:example{:example-id uuid?
                      :definition-id "clojure.core/disj"
                      :body "my example about this function."
                      :created-at inst?
                      :author #:author{:author-id uuid?
                                       :login "delboni"
                                       :account-source "github"
                                       :avatar-url "https://my.pic.com/me.jpg"
                                       :created-at inst?}
                      :editors [{:author/author-id uuid?
                                 :author/login "delboni"
                                 :author/account-source "github"
                                 :author/avatar-url "https://my.pic.com/me.jpg"
                                 :author/created-at inst?
                                 :editor/edited-at inst?}]}
            (util.db.postgres/get-example (:example/example-id example-1))))

  (flow "delete latest revision for current author part 3"
    (match? #:example{:example-id uuid?
                      :definition-id "clojure.core/disj"
                      :body "my example about this function."
                      :created-at inst?}
            (util.db.postgres/delete-example (:example/example-id example-1) author-id))

    (match? nil
            (util.db.postgres/get-example (:example/example-id example-1)))))

(defflow all-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}

  [author (util.db.postgres/upsert-author "delboni" "github")
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
    (match? {:social/definition-id "clojure.core/disj"
             :social/notes [#:note{:note-id uuid?
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
             :social/examples [#:example{:example-id uuid?
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
             :social/see-alsos [#:see-also{:see-also-id uuid?
                                           :definition-id "clojure.core/disj"
                                           :definition-id-to "clojure.core/dissoc"
                                           :created-at inst?
                                           :author #:author{:author-id uuid?
                                                            :login "delboni"
                                                            :account-source "github"
                                                            :avatar-url "https://my.pic.com/me.jpg"
                                                            :created-at inst?}}]}
            (util.db.postgres/get-by-definition "clojure.core/disj"))))
