(ns codes.clj.docs.backend.schemas.wire.out.document
  (:require [codes.clj.docs.backend.schemas.wire.document :refer [definition
                                                                  namespace-schema
                                                                  project]]
            [malli.util :as mu]))

(def Project
  (mu/select-keys project [:id
                           :name
                           :group
                           :artifact
                           :paths
                           :url
                           :sha
                           :tag
                           :manifest]))

(def Projects [:sequential Project])

(def Namespace
  (-> namespace-schema
      (mu/select-keys [:id
                       :name
                       :group
                       :artifact
                       :end-row
                       :meta
                       :name-end-col
                       :name-end-row
                       :name-row
                       :deprecated
                       :added
                       :author
                       :filename
                       :git-source
                       :col
                       :name-col
                       :end-col
                       :no-doc
                       :doc
                       :row])
      (mu/assoc :project-id :string)))

(def Namespaces [:sequential Namespace])

(def ProjectNamespaces
  [:map
   [:project Project]
   [:namespaces Namespaces]])

(def Definition
  (-> definition
      (mu/select-keys [:id
                       :group
                       :artifact
                       :name
                       :private
                       :deprecated
                       :added
                       :arglist-strs
                       :col
                       :defined-by
                       :doc
                       :end-col
                       :end-row
                       :filename
                       :fixed-arities
                       :git-source
                       :macro
                       :meta
                       :name-col
                       :name-end-col
                       :name-end-row
                       :name-row
                       :protocol-name
                       :protocol-ns
                       :row
                       :varargs-min-arity])
      (mu/assoc :namespace-id :string)))

(def Definitions [:sequential Definition])

(def ProjectNamespaceDefinitions
  [:map
   [:project Project]
   [:namespace Namespace]
   [:definitions Definitions]])

(def ProjectNamespaceDefinition
  [:map
   [:project Project]
   [:namespace Namespace]
   [:definition Definition]])

(def SearchResult
  [:map
   [:id :string]
   [:name :string]
   [:type [:enum :definition :namespace :project]]
   [:group {:optional true} :string]
   [:doc {:optional true} :string]])

(def SearchResults [:sequential SearchResult])
