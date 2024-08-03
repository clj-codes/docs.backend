(ns codes.clj.docs.backend.schemas.model.social
  (:require [codes.clj.docs.backend.schemas.types :refer [TimeInstant]]
            [malli.util :as mu]))

(def account-source [:enum "github"])

(def author
  [:map
   [:author/author-id :uuid]
   [:author/login :string]
   [:author/account-source account-source]
   [:author/avatar-url :string]
   [:author/created-at TimeInstant]])

(def NewAuthor
  (mu/select-keys author [:author/login
                          :author/account-source
                          :author/avatar-url]))

(def Author
  (mu/select-keys author [:author/author-id
                          :author/login
                          :author/account-source
                          :author/avatar-url
                          :author/created-at]))

(def Editor
  (mu/assoc Author
            :editor/edited-at TimeInstant))

(def see-also
  [:map
   [:see-also/see-also-id :uuid]
   [:see-also/author-id :uuid]
   [:see-also/author {:optional true} Author]
   [:see-also/definition-id :string]
   [:see-also/definition-id-to :string]
   [:see-also/created-at TimeInstant]])

(def NewSeeAlso
  (mu/select-keys see-also [:see-also/author-id
                            :see-also/definition-id
                            :see-also/definition-id-to]))

(def SeeAlso
  (mu/select-keys see-also [:see-also/see-also-id
                            :see-also/author
                            :see-also/definition-id
                            :see-also/definition-id-to
                            :see-also/created-at]))

(def example
  [:map
   [:example/example-id :uuid]
   [:example/author-id :uuid]
   [:example/author {:optional true} Author]
   [:example/editors {:optional true} [:sequential Editor]]
   [:example/definition-id :string]
   [:example/body :string]
   [:example/created-at TimeInstant]])

(def NewExample
  (mu/select-keys example [:example/author-id
                           :example/definition-id
                           :example/body]))

(def UpdateExample
  (mu/select-keys example [:example/example-id
                           :example/author-id
                           :example/body]))

(def Example
  (mu/select-keys example [:example/example-id
                           :example/author
                           :example/editors
                           :example/definition-id
                           :example/body
                           :example/created-at]))

(def note
  [:map
   [:note/note-id :uuid]
   [:note/author-id :uuid]
   [:note/author {:optional true} Author]
   [:note/definition-id :string]
   [:note/body :string]
   [:note/created-at TimeInstant]
   [:note/updated-at {:optional true} TimeInstant]])

(def NewNote
  (mu/select-keys note [:note/author-id
                        :note/definition-id
                        :note/body]))
(def UpdateNote
  (mu/select-keys note [:note/note-id
                        :note/author-id
                        :note/body]))

(def Note
  (mu/select-keys note [:note/note-id
                        :note/author
                        :note/definition-id
                        :note/body
                        :note/created-at
                        :note/updated-at]))

(def Social
  [:map
   [:social/definition-id :string]
   [:social/notes [:sequential Note]]
   [:social/examples [:sequential Example]]
   [:social/see-alsos [:sequential SeeAlso]]])

(def AnySocial
  [:or Example Note SeeAlso])

(def Author+Socials
  (mu/assoc Author [:author/socials {:optional true}] [:sequential Social]))

(def Author+Interactions
  (mu/assoc Author :author/interactions :int))
