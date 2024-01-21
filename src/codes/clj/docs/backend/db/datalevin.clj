(ns codes.clj.docs.backend.db.datalevin
  (:require [codes.clj.docs.backend.components.db-docs :as component.db-docs]
            [codes.clj.docs.backend.schemas.model.document :as schemas.model.document]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [datalevin.core :as d]))

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
