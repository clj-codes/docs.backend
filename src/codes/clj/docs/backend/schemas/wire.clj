(ns codes.clj.docs.backend.schemas.wire
  (:require [codes.clj.docs.backend.schemas.model.social :as model.social]
            [codes.clj.docs.backend.schemas.types :refer [TimeInstant]]
            [malli.util :as mu]))

; social
(def author
  [:map
   [:author-id :uuid]
   [:login :string]
   [:account-source model.social/account-source]
   [:avatar-url :string]
   [:created-at TimeInstant]])

(def Author
  (mu/select-keys author [:author-id
                          :login
                          :account-source
                          :avatar-url
                          :created-at]))

(def see-also
  [:map
   [:see-also-id :uuid]
   [:author-id :uuid]
   [:author {:optional true} Author]
   [:definition-id :string]
   [:definition-id-to :string]
   [:created-at TimeInstant]])

(def example
  [:map
   [:example-id :uuid]
   [:author-id :uuid]
   [:author {:optional true} Author]
   [:definition-id :string]
   [:body :string]
   [:created-at TimeInstant]])

(def note
  [:map
   [:note-id :uuid]
   [:author-id :uuid]
   [:author {:optional true} Author]
   [:definition-id :string]
   [:body :string]
   [:created-at TimeInstant]
   [:updated-at {:optional true} TimeInstant]])

; document
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
