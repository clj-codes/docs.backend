(ns codes.clj.docs.backend.adapters.document
  (:require [codes.clj.docs.backend.schemas.model.document :as schemas.model.document]
            [codes.clj.docs.backend.schemas.wire.out.document :as schemas.wire.out.document]
            [taoensso.encore :as enc]))

(defn project->wire
  {:malli/schema [:=> [:cat schemas.model.document/Project]
                  schemas.wire.out.document/Project]}
  [{:project/keys [id name group artifact paths url sha tag manifest]}]
  (enc/assoc-some {:id id
                   :name name}
                  :group   group
                  :artifact artifact
                  :paths   paths
                  :url     url
                  :sha     sha
                  :tag     tag
                  :manifest manifest))

(defn projects->wire
  {:malli/schema [:=> [:cat schemas.model.document/Projects]
                  schemas.wire.out.document/Projects]}
  [projects]
  (map project->wire projects))
