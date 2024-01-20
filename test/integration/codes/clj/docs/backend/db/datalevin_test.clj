(ns integration.codes.clj.docs.backend.db.datalevin-test
  (:require [clojure.test :refer [use-fixtures]]
            [codes.clj.docs.backend.components.db-docs :as component.db-docs]
            [datalevin.core :as d]
            [integration.codes.clj.docs.backend.fixtures.document :as fixtures.document]
            [integration.codes.clj.docs.backend.util :as util]
            [integration.codes.clj.docs.backend.util.db.datalevin :as util.db.datalevin]
            [parenthesin.helpers.malli :as helpers.malli]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defflow flow-integration-docs-definition-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    ; prepare docs-db with some data
    (util.db.datalevin/transact fixtures.document/datoms)

    ; TODO: remove this after endpoints are created
    (flow "find transacted datoms"
      [db-docs (state-flow.api/get-state :db-docs)]
      (match? {:db/id 3
               :definition/defined-by "clojure.core/defmacro"
               :definition/id "org.clojure/clojure/clojure.pprint/pprint-logical-block/0"
               :definition/filename "/src/clj/clojure/pprint/pprint_base.clj"
               :definition/macro true
               :definition/row 302
               :definition/varargs-min-arity 0
               :definition/added "1.2"
               :definition/arglist-strs ["[& args]"]
               :definition/col 1
               :definition/name-col 11
               :definition/end-col 16
               :definition/namespace #:namespace{:id "org.clojure/clojure/clojure.pprint"
                                                 :name "clojure.pprint"}
               :definition/doc "Execute the body as a pretty printing logical block with output to *out* which \nmust be a pretty printing writer. When used from pprint or cl-format this can be \nassumed. \n\nThis function is intended for use when writing custom dispatch functions.\n\nBefore the body the caller can optionally specify options: :prefix :per-line-prefix \nand :suffix."
               :definition/git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint/pprint_base.clj#L302"
               :definition/name-end-row 302
               :definition/name-row 302
               :definition/group "org.clojure"
               :definition/meta {:arglists '[[options* body]]}
               :definition/artifact "clojure"
               :definition/private false
               :definition/name-end-col 31
               :definition/end-row 327
               :definition/name "pprint-logical-block"}
              (d/q '[:find (pull ?e [*
                                     {:definition/namespace [*]}]) .
                     :in $ ?id
                     :where
                     [?e :definition/id ?id]]
                   (component.db-docs/db db-docs)
                   "org.clojure/clojure/clojure.pprint/pprint-logical-block/0")))))

(defflow projects-db-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}

  ; prepare docs-db with some data
  (util.db.datalevin/transact fixtures.document/datoms)

  (flow "find projects in db"
    (match? [{:project/manifest :pom
              :project/tag "clojure-1.11.1"
              :project/sha "ce55092f2b2f5481d25cff6205470c1335760ef6"
              :project/url "https://github.com/clojure/clojure"
              :project/artifact "clojure"
              :project/paths ["/src/main/java" "/src/main/clojure" "/src/resources" "/src/clj"]
              :project/name "org.clojure/clojure"
              :project/group "org.clojure"
              :project/id "org.clojure/clojure"}]
            (util.db.datalevin/get-projects))))
