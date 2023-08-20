(ns codes.clj.docs.backend.schemas.wire
  (:require [codes.clj.docs.backend.schemas.model.social :as model.social]
            [codes.clj.docs.backend.schemas.types :refer [TimeInstant]]
            [malli.util :as mu]))

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
