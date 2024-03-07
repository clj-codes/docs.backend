(ns codes.clj.docs.backend.ports.jwt
  (:require [buddy.core.hash :as hash]
            [buddy.sign.jwt :as jwt]
            [codes.clj.docs.backend.adapters.social :as adapters.social]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [codes.clj.docs.backend.schemas.wire.social :as schemas.wire.social]))

(defn encrypt
  {:malli/schema [:=> [:cat schemas.wire.social/Author schemas.types/GenericComponent]
                  :string]}
  [author config]
  (let [secret (-> config :config :jwt :secret hash/sha256)]
    (jwt/encrypt author secret {:alg :dir :enc :a128cbc-hs256})))

(defn decrypt
  {:malli/schema [:=> [:cat :string schemas.types/GenericComponent]
                  schemas.wire.social/Author]}
  [token config]
  (let [secret (-> config :config :jwt :secret hash/sha256)]
    (-> token
        (jwt/decrypt secret)
        adapters.social/jwt-author->wire)))
