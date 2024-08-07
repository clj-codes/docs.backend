(ns codes.clj.docs.backend.schemas.wire.out.social
  (:require [codes.clj.docs.backend.schemas.wire.social :refer [Author example
                                                                note see-also]]
            [malli.util :as mu]))

(def SeeAlso
  (mu/select-keys see-also [:see-also-id
                            :author
                            :definition-id
                            :definition-id-to
                            :created-at]))

(def Example
  (mu/select-keys example [:example-id
                           :author
                           :definition-id
                           :body
                           :created-at
                           :editors]))

(def Note
  (mu/select-keys note [:note-id
                        :author
                        :definition-id
                        :body
                        :created-at
                        :updated-at]))

(def Social
  [:map
   [:definition-id :string]
   [:notes [:sequential Note]]
   [:examples [:sequential Example]]
   [:see-alsos [:sequential SeeAlso]]])

(def AnySocial
  [:or Example Note SeeAlso])

(def Author+Socials
  (mu/assoc Author [:socials {:optional true}] [:sequential Social]))

(def Author+Interactions
  (mu/assoc Author :interactions :int))
