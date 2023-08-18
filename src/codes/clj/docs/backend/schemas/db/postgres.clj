(ns codes.clj.docs.backend.schemas.db.postgres
  (:require [codes.clj.docs.backend.schemas.types :refer [TimeInstant]]
            [malli.util :as mu]))

(def account-source [:enum "github"])

(def UnionRow
  [:map
   [:id :uuid]
   [:type [:enum "note" "example" "see-also"]]
   [:definition-id :string]
   [:body :string]
   [:created TimeInstant]
   [:author-id :uuid]
   [:login :string]
   [:account-source account-source]
   [:avatar-url :string]
   [:created-at TimeInstant]])

(def AuthorRow
  (mu/select-keys UnionRow [:author-id
                            :login
                            :account-source
                            :avatar-url
                            :created-at]))
