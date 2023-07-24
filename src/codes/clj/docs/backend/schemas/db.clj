(ns codes.clj.docs.backend.schemas.db
  (:require [malli.util :as mu]))

(def author
  [:map
   [:authors/author_id :uuid]
   [:authors/login :string]
   [:authors/account_source [:enum "github"]]
   [:authors/avatar_url :string]
   [:authors/created_at inst?]])

(def NewAuthor
  (mu/select-keys author [:authors/login
                          :authors/account_source
                          :authors/avatar_url]))

(def Author
  (mu/select-keys author [:authors/author_id
                          :authors/login
                          :authors/account_source
                          :authors/avatar_url
                          :authors/created_at]))
