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
   [:group :string]
   [:artifact :string]
   [:name :string]
   [:end-row {:optional true} :int]
   [:meta {:optional true} :any]
   [:name-end-col {:optional true} :int]
   [:name-end-row {:optional true} :int]
   [:name-row {:optional true} :int]
   [:added {:optional true} :string]
   [:author {:optional true} :string]
   [:filename {:optional true} :string]
   [:git-source {:optional true} :string]
   [:col {:optional true} :int]
   [:name-col {:optional true} :int]
   [:end-col {:optional true} :int]
   [:doc {:optional true} :string]
   [:row {:optional true} :int]])

(def definition
  [:map
   [:group :string]
   [:artifact :string]
   [:name :string]
   [:defined-by {:optional true} :string]
   [:namespace {:optional true} namespace-schema]
   [:fixed-arities {:optional true} [:sequential :int]]
   [:arglist-strs {:optional true} [:sequential :string]]
   [:end-row {:optional true} :int]
   [:meta {:optional true} :any]
   [:name-end-col {:optional true} :int]
   [:name-end-row {:optional true} :int]
   [:added {:optional true} :string]
   [:author {:optional true} :string]
   [:filename {:optional true} :string]
   [:git-source {:optional true} :string]
   [:col {:optional true} :int]
   [:name-col {:optional true} :int]
   [:end-col {:optional true} :int]
   [:macro {:optional true} :boolean]
   [:varargs-min-arity {:optional true} :int]
   [:private {:optional true} :boolean]
   [:protocol-ns {:optional true} :string]
   [:protocol-name {:optional true} :string]])
