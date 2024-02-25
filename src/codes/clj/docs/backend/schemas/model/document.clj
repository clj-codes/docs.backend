(ns codes.clj.docs.backend.schemas.model.document
  (:require [malli.util :as mu]))

(def Project
  [:map
   [:project/id :string]
   [:project/name :string]
   [:project/group :string]
   [:project/artifact :string]
   [:project/paths [:vector :string]]
   [:project/url :string]
   [:project/sha :string]
   [:project/tag :string]
   [:project/manifest :keyword]])

(def Projects [:sequential Project])

(def Namespace
  [:map
   [:namespace/id :string]
   [:namespace/project Project]
   [:namespace/name :string]
   [:namespace/group :string]
   [:namespace/artifact :string]
   [:namespace/name-end-col {:optional true} :int]
   [:namespace/deprecated {:optional true} :string]
   [:namespace/added {:optional true} :string]
   [:namespace/end-row {:optional true} :int]
   [:namespace/end-col {:optional true} :int]
   [:namespace/git-source {:optional true} :string]
   [:namespace/name-row {:optional true} :int]
   [:namespace/meta {:optional true} :any]
   [:namespace/row {:optional true} :int]
   [:namespace/col {:optional true} :int]
   [:namespace/author {:optional true} :string]
   [:namespace/name-col {:optional true} :int]
   [:namespace/no-doc {:optional true} :boolean]
   [:namespace/doc {:optional true} :string]
   [:namespace/name-end-row {:optional true} :int]
   [:namespace/filename {:optional true} :string]])

(def Namespaces [:sequential Namespace])

(def Definition
  [:map
   [:definition/id :string]
   [:definition/group :string]
   [:definition/artifact :string]
   [:definition/name :string]
   [:definition/namespace Namespace]
   [:definition/private :boolean]
   [:definition/deprecated {:optional true} :string]
   [:definition/added {:optional true} :string]
   [:definition/arglist-strs {:optional true} [:vector :string]]
   [:definition/col {:optional true} :int]
   [:definition/defined-by {:optional true} :string]
   [:definition/doc {:optional true} :string]
   [:definition/end-col {:optional true} :int]
   [:definition/end-row {:optional true} :int]
   [:definition/filename {:optional true} :string]
   [:definition/fixed-arities {:optional true} [:set :int]]
   [:definition/git-source {:optional true} :string]
   [:definition/macro {:optional true} :boolean]
   [:definition/meta {:optional true} :any]
   [:definition/name-col {:optional true} :int]
   [:definition/name-end-col {:optional true} :int]
   [:definition/name-end-row {:optional true} :int]
   [:definition/name-row {:optional true} :int]
   [:definition/protocol-name {:optional true} :string]
   [:definition/protocol-ns {:optional true} :string]
   [:definition/row {:optional true} :int]
   [:definition/varargs-min-arity {:optional true} :int]])

(def Definitions [:sequential Definition])

(def DefinitionSearchResult
  (mu/select-keys Definition [:definition/id
                              :definition/name
                              :definition/doc]))

(def NamespaceSearchResult
  (mu/select-keys Namespace [:namespace/id
                             :namespace/name
                             :namespace/doc]))

(def ProjectSearchResult
  (mu/select-keys Project [:project/id
                           :project/artifact
                           :project/group]))

(def SearchResult
  [:or DefinitionSearchResult NamespaceSearchResult ProjectSearchResult])
