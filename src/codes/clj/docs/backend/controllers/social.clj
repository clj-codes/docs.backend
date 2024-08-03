(ns codes.clj.docs.backend.controllers.social
  (:require [codes.clj.docs.backend.db.postgres :as db.postgres]
            [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.types :as schemas.types]))

(defn upsert-author
  {:malli/schema [:=> [:cat schemas.model.social/NewAuthor schemas.types/Components]
                  schemas.model.social/Author]}
  [new-author {:keys [database]}]
  (db.postgres/upsert-author new-author database))

(defn get-author+socials
  {:malli/schema [:=> [:cat :string schemas.model.social/account-source schemas.types/Components]
                  [:maybe schemas.model.social/Author+Socials]]}
  [login source {:keys [database]}]
  (db.postgres/get-author+socials login source database))

(defn insert-see-also
  {:malli/schema [:=> [:cat schemas.model.social/NewSeeAlso schemas.types/Components]
                  schemas.model.social/SeeAlso]}
  [new-see-also {:keys [database]}]
  (db.postgres/insert-see-also new-see-also database))

(defn delete-see-also
  {:malli/schema [:=> [:cat :uuid schemas.types/Components]
                  [:maybe schemas.model.social/SeeAlso]]}
  [see-also-id {:keys [database]}]
  (db.postgres/delete-see-also see-also-id database))

(defn get-see-also
  {:malli/schema [:=> [:cat :uuid schemas.types/Components]
                  [:maybe schemas.model.social/SeeAlso]]}
  [see-also-id {:keys [database]}]
  (db.postgres/get-see-also see-also-id database))

(defn insert-example
  {:malli/schema [:=> [:cat schemas.model.social/NewExample schemas.types/Components]
                  schemas.model.social/Example]}
  [new-example {:keys [database]}]
  (db.postgres/insert-example new-example database))

(defn update-example
  {:malli/schema [:=> [:cat schemas.model.social/UpdateExample schemas.types/Components]
                  schemas.model.social/Example]}
  [update-example {:keys [database]}]
  (db.postgres/update-example update-example database))

(defn delete-example
  {:malli/schema [:=> [:cat :uuid :uuid schemas.types/Components]
                  schemas.model.social/Example]}
  [example-id author-id {:keys [database]}]
  (db.postgres/delete-example example-id author-id database))

(defn get-example
  {:malli/schema [:=> [:cat :uuid schemas.types/Components]
                  [:maybe schemas.model.social/Example]]}
  [example-id {:keys [database]}]
  (db.postgres/get-example example-id database))

(defn insert-note
  {:malli/schema [:=> [:cat schemas.model.social/NewNote schemas.types/Components]
                  schemas.model.social/Note]}
  [new-note {:keys [database]}]
  (db.postgres/insert-note new-note database))

(defn update-note
  {:malli/schema [:=> [:cat schemas.model.social/UpdateNote schemas.types/Components]
                  schemas.model.social/Note]}
  [update-note {:keys [database]}]
  (db.postgres/update-note update-note database))

(defn delete-note
  {:malli/schema [:=> [:cat :uuid schemas.types/Components]
                  [:maybe schemas.model.social/Note]]}
  [note-id {:keys [database]}]
  (db.postgres/delete-note note-id database))

(defn get-note
  {:malli/schema [:=> [:cat :uuid schemas.types/Components]
                  [:maybe schemas.model.social/Note]]}
  [note-id {:keys [database]}]
  (db.postgres/get-note note-id database))

(defn get-by-definition
  {:malli/schema [:=> [:cat :string schemas.types/Components]
                  [:maybe schemas.model.social/Social]]}
  [definition-id {:keys [database]}]
  (db.postgres/get-by-definition definition-id database))

(defn get-top-authors
  {:malli/schema [:=> [:cat :int schemas.types/Components]
                  [:maybe [:sequential schemas.model.social/Author+Interactions]]]}
  [limit {:keys [database]}]
  (db.postgres/get-top-authors limit database))

(defn get-latest-interactions
  {:malli/schema [:=> [:cat :int schemas.types/Components]
                  [:maybe [:sequential schemas.model.social/AnySocial]]]}
  [limit {:keys [database]}]
  (db.postgres/get-latest-interactions limit database))
