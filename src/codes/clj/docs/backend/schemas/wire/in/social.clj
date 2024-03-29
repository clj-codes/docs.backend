(ns codes.clj.docs.backend.schemas.wire.in.social
  (:require [codes.clj.docs.backend.schemas.wire.social :refer [author example
                                                                note see-also]]
            [malli.util :as mu]))

(def NewAuthorGithub
  [:map
   [:login :string]
   [:avatar_url :string]])

(def JwtAuthor
  (-> author
      (mu/select-keys  [:account-source
                        :avatar-url
                        :login])
      (mu/assoc :author-id [:string {:gen/fmap (fn [_] (str (random-uuid)))}])
      (mu/assoc :created-at [:int {:min 1000}])))

(def NewSeeAlso
  (mu/select-keys see-also [:definition-id
                            :definition-id-to]))

(def NewExample
  (mu/select-keys example [:definition-id
                           :body]))

(def UpdateExample
  (mu/select-keys example [:example-id
                           :body]))

(def NewNote
  (mu/select-keys note [:definition-id
                        :body]))
(def UpdateNote
  (mu/select-keys note [:note-id
                        :body]))
