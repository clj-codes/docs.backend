(ns codes.clj.docs.backend.schemas.db.postgres
  (:require [codes.clj.docs.backend.schemas.types :refer [TimeInstant]]
            [malli.util :as mu]))

(def account-source [:enum "github"])

(def row
  [:map
   [:id :uuid]
   [:type [:enum "note" "example" "see-also"]]
   [:definition-id :string]
   [:body :string]
   [:created TimeInstant]
   [:author-id :uuid]
   [:note-id :uuid]
   [:example-id :uuid]
   [:see-also-id :uuid]
   [:login :string]
   [:account-source account-source]
   [:avatar-url :string]
   [:created-at TimeInstant]])

(def FullRow
  (mu/select-keys row [:id
                       :type
                       :definition-id
                       :body
                       :created
                       :author-id
                       :login
                       :account-source
                       :avatar-url
                       :created-at]))

(def BaseRow (mu/select-keys row [:id
                                  :definition-id
                                  :body
                                  :created]))

(def Row [:or FullRow BaseRow])

(def AuthorRow
  (mu/select-keys row [:author-id
                       :login
                       :account-source
                       :avatar-url
                       :created-at]))
