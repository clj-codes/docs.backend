(ns codes.clj.docs.backend.schemas.db
  (:require [malli.util :as mu]))

(def author
  [:map
   [:author/author-id :uuid]
   [:author/login :string]
   [:author/account-source [:enum "github"]]
   [:author/avatar-url :string]
   [:author/created-at inst?]])

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

(def see-also
  [:map
   [:see-also/see-also-id :uuid]
   [:see-also/author-id :uuid]
   [:see-also/definition-id :string]
   [:see-also/created-at inst?]])

(def NewSeeAlso
  (mu/select-keys see-also [:see-also/author-id
                            :see-also/definition-id]))

(def SeeAlso
  (mu/select-keys see-also [:see-also/see-also-id
                            :see-also/author-id
                            :see-also/definition-id
                            :see-also/created-at]))

(def example
  [:map
   [:example/example-id :uuid]
   [:example/author-id :uuid]
   [:example/definition-id :string]
   [:example/body :string]
   [:example/created-at inst?]])

(def NewExample
  (mu/select-keys example [:example/author-id
                           :example/definition-id
                           :example/body]))

(def Example
  (mu/select-keys example [:example/see-also-id
                           :example/author-id
                           :example/definition-id
                           :example/created-at]))

(def note
  [:map
   [:note/note-id :uuid]
   [:note/author-id :uuid]
   [:note/definition-id :string]
   [:note/body :string]
   [:note/created-at inst?]
   [:note/updated-at inst?]])

(def NewNote
  (mu/select-keys note [:note/author-id
                        :note/definition-id
                        :note/body]))

(def Note
  (mu/select-keys note [:note/see-also-id
                        :note/author-id
                        :note/definition-id
                        :note/created-at
                        :note/updated-at]))
