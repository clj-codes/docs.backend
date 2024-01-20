(ns integration.codes.clj.docs.backend.document-test
  (:require [clojure.test :refer [use-fixtures]]
            [integration.codes.clj.docs.backend.fixtures.document :as fixtures.document]
            [integration.codes.clj.docs.backend.util :as util]
            [integration.codes.clj.docs.backend.util.db.datalevin :as util.db.datalevin]
            [parenthesin.helpers.malli :as helpers.malli]
            [parenthesin.helpers.state-flow.server.pedestal :as state-flow.server]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defflow
  flow-integration-author-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    ; prepare docs-db with some data
    (util.db.datalevin/transact fixtures.document/datoms)

    (flow "should return projects"
      (match? {:status 200
               :body [{:group "org.clojure"
                       :name "org.clojure/clojure"
                       :paths ["/src/main/java"
                               "/src/main/clojure"
                               "/src/resources"
                               "/src/clj"]
                       :manifest "pom"
                       :id "org.clojure/clojure"
                       :url "https://github.com/clojure/clojure"
                       :artifact "clojure"
                       :tag "clojure-1.11.1"
                       :sha "ce55092f2b2f5481d25cff6205470c1335760ef6"}]}
              (state-flow.server/request! {:method :get
                                           :uri    "/document/projects/"})))))


