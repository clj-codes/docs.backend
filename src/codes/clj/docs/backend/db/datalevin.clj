(ns codes.clj.docs.backend.db.datalevin
  (:require [codes.clj.docs.backend.components.db-docs :as component.db-docs]
            [codes.clj.docs.backend.schemas.model.document :as schemas.model.document]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [datalevin.core :as d]
            [datalevin.interpret :refer [inter-fn]]
            [datalevin.search-utils :as su]))

(defn merge-tokenizers
  "Merges the results of tokenizer a and b into one sequence."
  [tokenizer-a tokenizer-b]
  (inter-fn [^String s]
    (into (sequence (tokenizer-a s))
      (sequence (tokenizer-b s)))))

(def read-conn-opts
  (let [query-analyzer (su/create-analyzer
                        {:tokenizer (merge-tokenizers
                                     (inter-fn [s] [[s 0 0]])
                                     (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+"))
                         :token-filters [su/lower-case-token-filter]})]
    {:search-domains {"project-name" {:query-analyzer query-analyzer}
                      "namespace-name" {:query-analyzer query-analyzer}
                      "definition-name" {:query-analyzer query-analyzer}}}))

(defn get-projects
  {:malli/schema [:=> [:cat schemas.types/DatalevinComponent]
                  schemas.model.document/Projects]}
  [db]
  (d/q '[:find [(pull ?p [*]) ...]
         :in $
         :where
         [?p :project/id]]
       (component.db-docs/db db)))

(defn get-namespaces-by-project
  {:malli/schema [:=> [:cat :string schemas.types/DatalevinComponent]
                  schemas.model.document/Namespaces]}
  [project-id db]
  (d/q '[:find [(pull ?n [* {:namespace/project [*]}]) ...]
         :in $ ?q
         :where
         [?p :project/id ?q]
         [?n :namespace/project ?p]]
       (component.db-docs/db db)
       project-id))

(defn get-definitions-by-namespace
  {:malli/schema [:=> [:cat :string schemas.types/DatalevinComponent]
                  schemas.model.document/Definitions]}
  [namespace-id db]
  (d/q '[:find [(pull ?d [* {:definition/namespace [* {:namespace/project [*]}]}]) ...]
         :in $ ?q
         :where
         [?p :namespace/id ?q]
         [?d :definition/namespace ?p]]
       (component.db-docs/db db)
       namespace-id))

(defn get-definition-by-id
  {:malli/schema [:=> [:cat :string schemas.types/DatalevinComponent]
                  [:maybe schemas.model.document/Definition]]}
  [definition-id db]
  (-> (d/q '[:find [(pull ?d [* {:definition/namespace [* {:namespace/project [*]}]}]) ...]
             :in $ ?q
             :where
             [?d :definition/id ?q]]
           (component.db-docs/db db)
           definition-id)
      first))

(defn search-by-fulltext
  {:malli/schema [:=> [:cat :string :int schemas.types/DatalevinComponent]
                  schemas.model.document/SearchResults]}
  [search top db-component]
  (let [db (component.db-docs/db db-component)]

    (->> (d/fulltext-datoms db
                            search
                            {:top top
                             :domains ["definition-name"
                                       "namespace-name"
                                       "project-name"]})
         (map first)
         (d/pull-many db '[:definition/id
                           :definition/name
                           :definition/doc
                           :namespace/id
                           :namespace/name
                           :namespace/doc
                           :project/id
                           :project/artifact
                           :project/group]))))
