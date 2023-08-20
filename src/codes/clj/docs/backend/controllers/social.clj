(ns codes.clj.docs.backend.controllers.social
  (:require [codes.clj.docs.backend.db.postgres :as db.postgres]
            [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.types :as schemas.types]))

(defn upsert-author
  {:malli/schema [:=> [:cat schemas.model.social/NewAuthor schemas.types/Components] schemas.model.social/Author]}
  [new-author {:keys [database]}]
  (db.postgres/upsert-author new-author database))

(defn get-author
  {:malli/schema [:=> [:cat :string schemas.model.social/account-source schemas.types/Components] schemas.model.social/Author]}
  [login source {:keys [database]}]
  (db.postgres/get-author login source database))

(defn insert-see-also
  {:malli/schema [:=> [:cat schemas.model.social/NewSeeAlso schemas.types/Components] schemas.model.social/SeeAlso]}
  [new-see-also {:keys [database]}]
  (db.postgres/insert-see-also new-see-also database))

(defn insert-example
  {:malli/schema [:=> [:cat schemas.model.social/NewExample schemas.types/Components] schemas.model.social/Example]}
  [new-example {:keys [database]}]
  (db.postgres/insert-example new-example database))

(defn update-example
  {:malli/schema [:=> [:cat schemas.model.social/UpdateExample schemas.types/Components] schemas.model.social/Example]}
  [update-example {:keys [database]}]
  (db.postgres/update-example update-example database))

(defn insert-note
  {:malli/schema [:=> [:cat schemas.model.social/NewNote schemas.types/Components] schemas.model.social/Note]}
  [new-note {:keys [database]}]
  (db.postgres/insert-note new-note database))

(defn update-note
  {:malli/schema [:=> [:cat schemas.model.social/UpdateNote schemas.types/Components] schemas.model.social/Note]}
  [update-note {:keys [database]}]
  (db.postgres/update-note update-note database))

(defn get-by-definition
  {:malli/schema [:=> [:cat :string schemas.types/DatabaseComponent] [:sequential schemas.model.social/Definition]]}
  [definition-id {:keys [database]}]
  (db.postgres/get-by-definition definition-id database))
