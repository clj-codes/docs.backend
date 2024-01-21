(ns codes.clj.docs.backend.schemas.wire.document)

(def project
  [:map
   [:id :string]
   [:name :string]
   [:group {:optional true} :string]
   [:artifact {:optional true} :string]
   [:paths {:optional true} [:sequential :string]]
   [:url {:optional true} :string]
   [:sha {:optional true} :string]
   [:tag {:optional true} :string]
   [:manifest {:optional true} :keyword]])

(def namespace-schema
  [:map
   [:id :string]
   [:project project]
   [:name :string]
   [:group :string]
   [:artifact :string]
   [:name-end-col {:optional true} :int]
   [:added {:optional true} :string]
   [:end-row {:optional true} :int]
   [:end-col {:optional true} :int]
   [:git-source {:optional true} :string]
   [:name-row {:optional true} :int]
   [:meta {:optional true} :any]
   [:row {:optional true} :int]
   [:col {:optional true} :int]
   [:author {:optional true} :string]
   [:name-col {:optional true} :int]
   [:doc {:optional true} :string]
   [:name-end-row {:optional true} :int]
   [:filename {:optional true} :string]])

(def definition
  [:map
   [:id :string]
   [:group :string]
   [:artifact :string]
   [:name :string]
   [:namespace namespace-schema]
   [:private :boolean]
   [:added {:optional true} :string]
   [:arglist-strs {:optional true} [:vector :string]]
   [:col {:optional true} :int]
   [:defined-by {:optional true} :string]
   [:doc {:optional true} :string]
   [:end-col {:optional true} :int]
   [:end-row {:optional true} :int]
   [:filename {:optional true} :string]
   [:fixed-arities {:optional true} [:set :int]]
   [:git-source {:optional true} :string]
   [:macro {:optional true} :boolean]
   [:meta {:optional true} :any]
   [:name-col {:optional true} :int]
   [:name-end-col {:optional true} :int]
   [:name-end-row {:optional true} :int]
   [:name-row {:optional true} :int]
   [:protocol-name {:optional true} :string]
   [:protocol-ns {:optional true} :string]
   [:row {:optional true} :int]
   [:varargs-min-arity {:optional true} :int]])
