(ns codes.clj.docs.backend.schemas.db
  (:require [malli.util :as mu]))

(def author
  [:map
   [:author/author_id :uuid]
   [:author/login :string]
   [:author/account_source [:enum "github"]]
   [:author/avatar_url :string]
   [:author/created_at inst?]])

(def NewAuthor
  (mu/select-keys author [:author/login
                          :author/account_source
                          :author/avatar_url]))

(def Author
  (mu/select-keys author [:author/author_id
                          :author/login
                          :author/account_source
                          :author/avatar_url
                          :author/created_at]))

(def see-also
  [:map
   [:see_also/see_also_id :uuid]
   [:see_also/author_id :uuid]
   [:see_also/definition_id :string]
   [:see_also/created_at inst?]])

(def NewSeeAlso
  (mu/select-keys author [:see_also/author_id
                          :see_also/definition_id]))

(def SeeAlso
  (mu/select-keys author [:see_also/see_also_id
                          :see_also/author_id
                          :see_also/definition_id
                          :see_also/created_at]))

(def note
  [:map
   [:note/note_id :uuid]
   [:note/author_id :uuid]
   [:note/definition_id :string]
   [:note/body :string]
   [:note/created_at inst?]
   [:note/updated_at inst?]])

(def NewNote
  (mu/select-keys author [:note/author_id
                          :note/definition_id
                          :note/body]))

(def Note
  (mu/select-keys author [:note/see_also_id
                          :note/author_id
                          :note/definition_id
                          :note/created_at]))
