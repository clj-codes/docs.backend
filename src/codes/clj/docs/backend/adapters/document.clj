(ns codes.clj.docs.backend.adapters.document
  (:require [codes.clj.docs.backend.schemas.model.document :as schemas.model.document]
            [codes.clj.docs.backend.schemas.wire.out.document :as schemas.wire.out.document]
            [taoensso.encore :as enc]))

(defn project->wire
  {:malli/schema [:=> [:cat schemas.model.document/Project]
                  schemas.wire.out.document/Project]}
  [{:project/keys [id name
                   group artifact paths url sha tag manifest]}]
  (enc/assoc-some {:id   id
                   :name name}
                  :group    group
                  :artifact artifact
                  :paths    paths
                  :url      url
                  :sha      sha
                  :tag      tag
                  :manifest manifest))

(defn projects->wire
  {:malli/schema [:=> [:cat schemas.model.document/Projects]
                  schemas.wire.out.document/Projects]}
  [projects]
  (map project->wire projects))

(defn namespace->wire
  {:malli/schema [:=> [:cat schemas.model.document/Namespace]
                  schemas.wire.out.document/Namespace]}
  [{:namespace/keys [id name group artifact project
                     end-row meta name-end-col name-end-row name-row added
                     author filename git-source col name-col end-col doc row]}]
  (enc/assoc-some
   {:id           id
    :name         name
    :group        group
    :artifact     artifact
    :project-id   (:project/id project)}
   :name-end-col name-end-col
   :added        added
   :end-row      end-row
   :end-col      end-col
   :git-source   git-source
   :name-row     name-row
   :meta         meta
   :row          row
   :col          col
   :author       author
   :name-col     name-col
   :doc          doc
   :name-end-row name-end-row
   :filename     filename))

(defn namespaces->wire
  {:malli/schema [:=> [:cat schemas.model.document/Namespaces]
                  schemas.wire.out.document/Namespaces]}
  [namespaces]
  (map namespace->wire namespaces))

(defn definition->wire
  {:malli/schema [:=> [:cat schemas.model.document/Definition]
                  schemas.wire.out.document/Definition]}
  [{:definition/keys [id group artifact name namespace
                      private added arglist-strs col defined-by doc end-col
                      end-row filename fixed-arities git-source macro meta
                      name-col name-end-col name-end-row name-row protocol-name
                      protocol-ns row varargs-min-arity]}]
  (enc/assoc-some
   {:id        id
    :group     group
    :artifact  artifact
    :name      name
    :namespace-id (:namespace/id namespace)}
   :private           private
   :added             added
   :arglist-strs      arglist-strs
   :col               col
   :defined-by        defined-by
   :doc               doc
   :end-col           end-col
   :end-row           end-row
   :filename          filename
   :fixed-arities     fixed-arities
   :git-source        git-source
   :macro             macro
   :meta              meta
   :name-col          name-col
   :name-end-col      name-end-col
   :name-end-row      name-end-row
   :name-row          name-row
   :protocol-name     protocol-name
   :protocol-ns       protocol-ns
   :row               row
   :varargs-min-arity varargs-min-arity))

(defn definitions->wire
  {:malli/schema [:=> [:cat schemas.model.document/Definitions]
                  schemas.wire.out.document/Definitions]}
  [definitions]
  (map definition->wire definitions))
