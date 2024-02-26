(ns codes.clj.docs.backend.controllers.document
  (:require [codes.clj.docs.backend.db.datalevin :as db]
            [codes.clj.docs.backend.schemas.model.document :as schemas.model.document]
            [codes.clj.docs.backend.schemas.types :as schemas.types]))

(defn get-projects
  {:malli/schema [:=> [:cat schemas.types/Components]
                  schemas.model.document/Projects]}
  [{:keys [db-docs]}]
  (db/get-projects db-docs))

(defn get-namespaces-by-project
  {:malli/schema [:=> [:cat :string schemas.types/Components]
                  schemas.model.document/Namespaces]}
  [project-id {:keys [db-docs]}]
  (db/get-namespaces-by-project project-id db-docs))

(defn get-definitions-by-namespace
  {:malli/schema [:=> [:cat :string schemas.types/Components]
                  schemas.model.document/Definitions]}
  [namespace-id {:keys [db-docs]}]
  (db/get-definitions-by-namespace namespace-id db-docs))

(defn get-definition-by-id
  {:malli/schema [:=> [:cat :string schemas.types/Components]
                  [:maybe schemas.model.document/Definition]]}
  [definition-id {:keys [db-docs]}]
  (db/get-definition-by-id definition-id db-docs))

(defn search-by-fulltext
  {:malli/schema [:=> [:cat :string :int schemas.types/DatalevinComponent]
                  schemas.model.document/SearchResults]}
  [search top {:keys [db-docs]}]
  (db/search-by-fulltext search top db-docs))
