(ns codes.clj.docs.backend.controllers.social.github
  (:require [codes.clj.docs.backend.ports.http-out :as ports.http-out]
            [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.types :as schemas.types]))

(defn get-author
  {:malli/schema [:=> [:cat :string schemas.types/Components]
                  schemas.model.social/NewAuthor]}
  [code components]
  (-> code
      (ports.http-out/github-exchange-code->access-token components)
      (ports.http-out/github-get-user-info components)))
