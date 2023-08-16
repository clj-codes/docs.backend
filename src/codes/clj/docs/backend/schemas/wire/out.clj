(ns codes.clj.docs.backend.schemas.wire.out
  (:require [codes.clj.docs.backend.schemas.wire :refer [example note see-also]]
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
                           :created-at]))

(def Note
  (mu/select-keys note [:note-id
                        :author
                        :definition-id
                        :body
                        :created-at
                        :updated-at]))

(def Definition
  [:map
   [:definition-id :string]
   [:notes [:sequential Note]]
   [:examples [:sequential Example]]
   [:see-alsos [:sequential SeeAlso]]])
