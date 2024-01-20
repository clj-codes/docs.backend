(ns codes.clj.docs.backend.db.datalevin
  (:require [codes.clj.docs.backend.components.db-docs :as component.db-docs]
            [codes.clj.docs.backend.schemas.model.document :as schemas.model.document]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [datalevin.core :as d]))

(defn get-projects
  {:malli/schema [:=> [:cat schemas.types/DatalevinComponent]
                  schemas.model.document/Projects]}
  [db]
  (d/q '[:find [(pull ?e [*]) ...]
         :in $
         :where
         [?e :project/id]]
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
