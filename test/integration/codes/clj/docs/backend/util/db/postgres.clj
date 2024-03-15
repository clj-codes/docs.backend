(ns integration.codes.clj.docs.backend.util.db.postgres
  (:require [codes.clj.docs.backend.db.postgres :as db]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn upsert-author
  [login source]
  (flow "insert data in the database and return"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/upsert-author {:author/login login
                            :author/account-source source
                            :author/avatar-url "https://my.pic.com/me.jpg"})
         state-flow.api/return)))

(defn create-example
  [example]
  (flow "insert new example"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/insert-example example)
         state-flow.api/return)))

(defn update-example
  [example]
  (flow "update example"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/update-example example)
         state-flow.api/return)))

(defn create-note
  [note]
  (flow "insert new note"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/insert-note note)
         state-flow.api/return)))

(defn update-note
  [note]
  (flow "update note"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/update-note note)
         state-flow.api/return)))

(defn delete-note
  [note-id]
  (flow "delete note"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/delete-note note-id)
         state-flow.api/return)))

(defn create-see-also
  [see-also]
  (flow "insert new see-also"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/insert-see-also see-also)
         state-flow.api/return)))

(defn delete-see-also
  [see-also-id]
  (flow "delete see-also"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/delete-see-also see-also-id)
         state-flow.api/return)))

(defn get-note
  [note-id]
  (flow "get note by id"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/get-note note-id)
         state-flow.api/return)))

(defn get-see-also
  [see-also-id]
  (flow "get see-also by id"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/get-see-also see-also-id)
         state-flow.api/return)))

(defn get-example
  [example-id]
  (flow "get example by id"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/get-example example-id)
         state-flow.api/return)))

(defn get-by-definition
  [definition-id]
  (flow "get socials by definition id"
    [database (state-flow.api/get-state :database)]
    (->> database
         (db/get-by-definition definition-id)
         state-flow.api/return)))
