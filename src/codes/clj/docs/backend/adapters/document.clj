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
                     end-row meta name-end-col name-end-row name-row added author filename git-source col name-col end-col doc row]}]
  (enc/assoc-some {:id         id
                   :group      group
                   :artifact   artifact
                   :name       name
                   :project-id (:project/id project)}
                  :end-row      end-row
                  :meta         meta
                  :name-end-col name-end-col
                  :name-end-row name-end-row
                  :name-row     name-row
                  :added        added
                  :author       author
                  :filename     filename
                  :git-source   git-source
                  :col          col
                  :name-col     name-col
                  :end-col      end-col
                  :doc          doc
                  :row          row))

(defn namespaces->wire
  {:malli/schema [:=> [:cat schemas.model.document/Namespaces]
                  schemas.wire.out.document/Namespaces]}
  [namespaces]
  (map namespace->wire namespaces))

(defn definition->wire
  {:malli/schema [:=> [:cat schemas.model.document/Definition]
                  schemas.wire.out.document/Definition]}
  [{:definition/keys [id name group artifact namespace
                      defined-by fixed-arities arglist-strs end-row meta name-end-col name-end-row added author filename git-source col name-col end-col macro varargs-min-arity private protocol-ns protocol-name]}]
  (enc/assoc-some
   {:id                id
    :group             group
    :artifact          artifact
    :name              name
    :namespace-id      (:namespace/id namespace)}
   :defined-by        defined-by
   :fixed-arities     fixed-arities
   :arglist-strs      arglist-strs
   :end-row           end-row
   :meta              meta
   :name-end-col      name-end-col
   :name-end-row      name-end-row
   :added             added
   :author            author
   :filename          filename
   :git-source        git-source
   :col               col
   :name-col          name-col
   :end-col           end-col
   :macro             macro
   :varargs-min-arity varargs-min-arity
   :private           private
   :protocol-ns       protocol-ns
   :protocol-name     protocol-name))

(defn definitions->wire
  {:malli/schema [:=> [:cat schemas.model.document/Definitions]
                  schemas.wire.out.document/Definitions]}
  [definitions]
  (map definition->wire definitions))
