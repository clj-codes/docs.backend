(ns integration.codes.clj.docs.backend.util.db.datalevin
  (:require [codes.clj.docs.backend.components.db-docs :as component.db-docs]
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
