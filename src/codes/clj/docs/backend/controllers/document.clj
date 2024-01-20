(ns codes.clj.docs.backend.controllers.document
  (:require [codes.clj.docs.backend.db.datalevin :as db]
            [codes.clj.docs.backend.schemas.model.document :as schemas.model.document]
            [codes.clj.docs.backend.schemas.types :as schemas.types]))

(defn get-projects
  {:malli/schema [:=> [:cat schemas.types/Components] schemas.model.document/Projects]}
  [{:keys [db-docs]}]
  (db/get-projects db-docs))
