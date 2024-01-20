(ns codes.clj.docs.backend.schemas.model.document)

(def Project
  [:map
   [:project/id :string]
   [:project/name :string]
   [:project/group {:optional true} :string]
   [:project/artifact {:optional true} :string]
   [:project/paths {:optional true} [:sequential :string]]
   [:project/url {:optional true} :string]
   [:project/sha {:optional true} :string]
   [:project/tag {:optional true} :string]
   [:project/manifest {:optional true} :keyword]])

(def Projects [:sequential Project])

(def Namespace
  [:map
   [:namespace/id :string]
   [:namespace/project Project]
   [:namespace/group :string]
   [:namespace/artifact :string]
   [:namespace/name :string]
   [:namespace/end-row {:optional true} :int]
   [:namespace/meta {:optional true} :any]
   [:namespace/name-end-col {:optional true} :int]
   [:namespace/name-end-row {:optional true} :int]
   [:namespace/name-row {:optional true} :int]
   [:namespace/added {:optional true} :string]
   [:namespace/author {:optional true} :string]
   [:namespace/filename {:optional true} :string]
   [:namespace/git-source {:optional true} :string]
   [:namespace/col {:optional true} :int]
   [:namespace/name-col {:optional true} :int]
   [:namespace/end-col {:optional true} :int]
   [:namespace/doc {:optional true} :string]
   [:namespace/row {:optional true} :int]])

(def Namespaces [:sequential Namespace])

(def Definition
  [:map
   [:definition/group :string]
   [:definition/artifact :string]
   [:definition/name :string]
   [:definition/defined-by {:optional true} :string]
   [:definition/namespace {:optional true} Namespace]
   [:definition/fixed-arities {:optional true} [:sequential :int]]
   [:definition/arglist-strs {:optional true} [:sequential :string]]
   [:definition/end-row {:optional true} :int]
   [:definition/meta {:optional true} :any]
   [:definition/name-end-col {:optional true} :int]
   [:definition/name-end-row {:optional true} :int]
   [:definition/added {:optional true} :string]
   [:definition/author {:optional true} :string]
   [:definition/filename {:optional true} :string]
   [:definition/git-source {:optional true} :string]
   [:definition/col {:optional true} :int]
   [:definition/name-col {:optional true} :int]
   [:definition/end-col {:optional true} :int]
   [:definition/macro {:optional true} :boolean]
   [:definition/varargs-min-arity {:optional true} :int]
   [:definition/private {:optional true} :boolean]
   [:definition/protocol-ns {:optional true} :string]
   [:definition/protocol-name {:optional true} :string]])
