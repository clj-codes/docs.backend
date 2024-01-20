(ns codes.clj.docs.backend.db.datalevin
  (:require [codes.clj.docs.backend.components.db-docs :as component.db-docs]
            [codes.clj.docs.backend.schemas.model.document :as schemas.model.document]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [datalevin.core :as d]))

(defn get-projects
  {:malli/schema [:=> [:cat schemas.types/DatalevinComponent] [:sequential schemas.model.document/Project]]}
  [db]
  (d/q '[:find [(pull ?e [*]) ...]
         :in $
         :where
         [?e :project/id]]
       (component.db-docs/db db)))
