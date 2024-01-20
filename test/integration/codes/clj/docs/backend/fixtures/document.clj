(ns integration.codes.clj.docs.backend.fixtures.document)

(def project-clojure
  #:project{:manifest :pom
            :tag "clojure-1.11.1"
            :sha "ce55092f2b2f5481d25cff6205470c1335760ef6"
            :url "https://github.com/clojure/clojure"
            :artifact "clojure"
            :paths ["/src/main/java"
                    "/src/main/clojure"
                    "/src/resources"
                    "/src/clj"]
            :name "org.clojure/clojure"
            :group "org.clojure"
            :id "org.clojure/clojure"})

(def namespace-clojure-pprint
  #:namespace{:artifact "clojure"
              :project #:project{:id "org.clojure/clojure"}
              :name-end-col 19
              :added "1.2"
              :end-row 39
              :end-col 40
              :git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint.clj#L14"
              :name-row 37
              :meta {}
              :row 14
              :col 1
              :author "Tom Faulhaber"
              :name-col 5
              :name "clojure.pprint"
              :doc "A Pretty Printer for Clojure\n\nclojure.pprint implements a flexible system for printing structured data\nin a pleasing easy-to-understand format. Basic use of the pretty printer is \nsimple just call pprint instead of println. More advanced users can use \nthe building blocks provided to create custom output formats. \n\nOut of the box pprint supports a simple structured format for basic data \nand a specialized format for Clojure source code. More advanced formats \nincluding formats that don't look like Clojure data at all like XML and \nJSON can be rendered by creating custom dispatch functions. \n\nIn addition to the pprint function this module contains cl-format a text \nformatting function which is fully compatible with the format function in \nCommon Lisp. Because pretty printing directives are directly integrated with\ncl-format it supports very concise custom dispatch. It also provides\na more powerful alternative to Clojure's standard format function.\n\nSee documentation for pprint and cl-format for more information or \ncomplete documentation on the Clojure web site on GitHub."
              :id "org.clojure/clojure/clojure.pprint"
              :name-end-row 37
              :filename "/src/clj/clojure/pprint.clj"
              :group "org.clojure"})

(def definition-clojure-pprint-pprint-logical-block
  #:definition{:defined-by "clojure.core/defmacro"
               :id "org.clojure/clojure/clojure.pprint/pprint-logical-block/0"
               :filename "/src/clj/clojure/pprint/pprint_base.clj"
               :macro true
               :row 302
               :varargs-min-arity 0
               :added "1.2"
               :arglist-strs ["[& args]"]
               :col 1
               :name-col 11
               :end-col 16
               :namespace #:namespace{:id "org.clojure/clojure/clojure.pprint"}
               :doc "Execute the body as a pretty printing logical block with output to *out* which \nmust be a pretty printing writer. When used from pprint or cl-format this can be \nassumed. \n\nThis function is intended for use when writing custom dispatch functions.\n\nBefore the body the caller can optionally specify options: :prefix :per-line-prefix \nand :suffix."
               :git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint/pprint_base.clj#L302"
               :name-end-row 302
               :name-row 302
               :group "org.clojure"
               :meta {:arglists '[[options* body]]}
               :artifact "clojure"
               :private false
               :name-end-col 31
               :end-row 327
               :name "pprint-logical-block"})

(def definition-clojure-pprint-print-table
  #:definition{:defined-by "clojure.core/defn"
               :id "org.clojure/clojure/clojure.pprint/print-table/0"
               :filename "/src/clj/clojure/pprint/print_table.clj"
               :row 11
               :added "1.3"
               :arglist-strs ["[ks rows]" "[rows]"]
               :col 1
               :name-col 7
               :end-col 51
               :namespace #:namespace{:id "org.clojure/clojure/clojure.pprint"}
               :doc "Prints a collection of maps in a textual table. Prints table headings\n   ks and then a line of output for each row corresponding to the keys\n   in ks. If ks are not specified use the keys of the first item in rows."
               :git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint/print_table.clj#L11"
               :fixed-arities #{1 2}
               :name-end-row 11
               :name-row 11
               :group "org.clojure"
               :meta {}
               :artifact "clojure"
               :private false
               :name-end-col 18
               :end-row 35
               :name "print-table"})

(def datoms
  [project-clojure
   namespace-clojure-pprint
   definition-clojure-pprint-pprint-logical-block
   definition-clojure-pprint-print-table])
