(ns integration.codes.clj.docs.backend.util.db.datalevin
  (:require [codes.clj.docs.backend.components.db-docs :as component.db-docs]
            [codes.clj.docs.backend.db.datalevin :as db]
            [datalevin.core :as d]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn transact
  [datoms]
  (flow "transact datatoms in the database and return"
    [db-docs (state-flow.api/get-state :db-docs)]
    (->> (d/transact! (component.db-docs/conn db-docs)
                      datoms)
         state-flow.api/return)))

(defn get-projects
  []
  (flow "get all projects in document db"
    [database (state-flow.api/get-state :db-docs)]
    (->> database
         (db/get-projects)
         state-flow.api/return)))

(defn get-namespaces-by-project
  [project-id]
  (flow "get all namespaces for a project-id in document db"
    [database (state-flow.api/get-state :db-docs)]
    (->> database
         (db/get-namespaces-by-project project-id)
         state-flow.api/return)))

(defn get-definition-by-namespace
  [namespace-id]
  (flow "get all definitions for a namespace-id in document db"
    [database (state-flow.api/get-state :db-docs)]
    (->> database
         (db/get-definitions-by-namespace namespace-id)
         state-flow.api/return)))

(defn get-definition-by-id
  [definition-id]
  (flow "get definition by id in document db"
    [database (state-flow.api/get-state :db-docs)]
    (->> database
         (db/get-definition-by-id definition-id)
         state-flow.api/return)))
