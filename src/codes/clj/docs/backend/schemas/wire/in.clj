(ns codes.clj.docs.backend.schemas.wire.in
  (:require [codes.clj.docs.backend.schemas.wire :refer [author example note
                                                         see-also]]
            [malli.util :as mu]))

(def NewAuthor
  (mu/select-keys author [:login
                          :account-source
                          :avatar-url]))

(def NewSeeAlso
  (mu/select-keys see-also [:author-id
                            :definition-id
                            :definition-id-to]))

(def NewExample
  (mu/select-keys example [:author-id
                           :definition-id
                           :body]))

(def UpdateExample
  (mu/select-keys example [:example-id
                           :author-id
                           :body]))

(def NewNote
  (mu/select-keys note [:author-id
                        :definition-id
                        :body]))
(def UpdateNote
  (mu/select-keys note [:note-id
                        :author-id
                        :definition-id
                        :body]))
