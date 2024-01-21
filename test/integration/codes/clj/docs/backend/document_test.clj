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
  flow-integration-document-test
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
                                           :uri    "/document/projects/"})))

    (flow "should return namespaces"
      (match? {:status 200
               :body [{:end-row 39
                       :group "org.clojure"
                       :meta {}
                       :name-end-col 19
                       :name-end-row 37
                       :name-row 37
                       :added "1.2"
                       :name "clojure.pprint"
                       :git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint.clj#L14"
                       :author "Tom Faulhaber"
                       :filename "/src/clj/clojure/pprint.clj"
                       :col 1
                       :id "org.clojure/clojure/clojure.pprint"
                       :name-col 5
                       :artifact "clojure"
                       :end-col 40
                       :project-id "org.clojure/clojure"
                       :doc "A Pretty Printer for Clojure\n\nclojure.pprint implements a flexible system for printing structured data\nin a pleasing easy-to-understand format. Basic use of the pretty printer is \nsimple just call pprint instead of println. More advanced users can use \nthe building blocks provided to create custom output formats. \n\nOut of the box pprint supports a simple structured format for basic data \nand a specialized format for Clojure source code. More advanced formats \nincluding formats that don't look like Clojure data at all like XML and \nJSON can be rendered by creating custom dispatch functions. \n\nIn addition to the pprint function this module contains cl-format a text \nformatting function which is fully compatible with the format function in \nCommon Lisp. Because pretty printing directives are directly integrated with\ncl-format it supports very concise custom dispatch. It also provides\na more powerful alternative to Clojure's standard format function.\n\nSee documentation for pprint and cl-format for more information or \ncomplete documentation on the Clojure web site on GitHub."
                       :row 14}]}
              (state-flow.server/request! {:method :get
                                           :uri    "/document/namespaces/org.clojure/clojure"})))
    (flow "should not return namespaces"
      (match? {:status 404
               :body "not found"}
              (state-flow.server/request! {:method :get
                                           :uri    "/document/namespaces/golang/go"})))

    (flow "should return definitions"
      (match? {:status 200
               :body [{:fixed-arities [1 2]
                       :end-row 35
                       :group "org.clojure"
                       :meta {}
                       :namespace-id "org.clojure/clojure/clojure.pprint"
                       :name-end-col 18
                       :name-end-row 11
                       :private false
                       :added "1.3"
                       :name "print-table"
                       :defined-by "clojure.core/defn"
                       :git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint/print_table.clj#L11"
                       :filename "/src/clj/clojure/pprint/print_table.clj"
                       :col 1
                       :name-col 7
                       :artifact "clojure"
                       :end-col 51
                       :arglist-strs ["[ks rows]" "[rows]"]}
                      {:end-row 327
                       :group "org.clojure"
                       :meta {:arglists [["options*" "body"]]}
                       :namespace-id "org.clojure/clojure/clojure.pprint"
                       :name-end-col 31
                       :name-end-row 302
                       :private false
                       :added "1.2"
                       :name "pprint-logical-block"
                       :defined-by "clojure.core/defmacro"
                       :git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint/pprint_base.clj#L302"
                       :filename "/src/clj/clojure/pprint/pprint_base.clj"
                       :macro true
                       :col 1
                       :name-col 11
                       :artifact "clojure"
                       :end-col 16
                       :arglist-strs ["[& args]"]
                       :varargs-min-arity 0}]}
              (state-flow.server/request! {:method :get
                                           :uri    "/document/definitions/org.clojure/clojure/clojure.pprint"})))
    (flow "should not return definitions"
      (match? {:status 404
               :body "not found"}
              (state-flow.server/request! {:method :get
                                           :uri    "/document/definitions/golang/go/math"})))))
