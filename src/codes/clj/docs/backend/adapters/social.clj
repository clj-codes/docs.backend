(ns codes.clj.docs.backend.adapters.social
  (:require [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.wire.in.social :as schemas.wire.in.social]
            [codes.clj.docs.backend.schemas.wire.out.social :as schemas.wire.out.social]
            [codes.clj.docs.backend.schemas.wire.social :as schemas.wire.social]
            [taoensso.encore :as enc])
  (:import [java.time Instant]))

(defn github-user-wire->model
  {:malli/schema [:=> [:cat schemas.wire.in.social/NewAuthorGithub]
                  schemas.model.social/NewAuthor]}
  [{:keys [login avatar_url]}]
  #:author{:login login
           :account-source "github"
           :avatar-url avatar_url})

(defn jwt-author->wire
  {:malli/schema [:=> [:cat schemas.wire.in.social/JwtAuthor]
                  schemas.wire.social/Author]}
  [{:keys [author-id created-at login account-source avatar-url]}]
  {:author-id (parse-uuid author-id)
   :created-at (Instant/ofEpochMilli created-at)
   :login login
   :account-source account-source
   :avatar-url avatar-url})

(defn author->model->wire
  {:malli/schema [:=> [:cat schemas.model.social/Author] schemas.wire.social/Author]}
  [{:author/keys [author-id login account-source avatar-url created-at]}]
  {:author-id author-id
   :login login
   :account-source account-source
   :avatar-url avatar-url
   :created-at created-at})

(defn new-example-wire->model
  {:malli/schema [:=> [:cat schemas.wire.in.social/NewExample :uuid]
                  schemas.model.social/NewExample]}
  [{:keys [definition-id body]} author-id]
  #:example{:author-id author-id
            :definition-id definition-id
            :body body})

(defn update-example-wire->model
  {:malli/schema [:=> [:cat schemas.wire.in.social/UpdateExample :uuid]
                  schemas.model.social/UpdateExample]}
  [{:keys [example-id body]} author-id]
  #:example{:example-id example-id
            :author-id author-id
            :body body})

(defn example->model->wire
  {:malli/schema [:=> [:cat schemas.model.social/Example]
                  schemas.wire.out.social/Example]}
  [{:example/keys [example-id author definition-id body created-at editors]}]
  (enc/assoc-some {:example-id example-id
                   :definition-id definition-id
                   :body body
                   :created-at created-at}
                  :author (when author (author->model->wire author))
                  :editors (when (seq editors)
                             (map author->model->wire editors))))

(defn new-see-also-wire->model
  {:malli/schema [:=> [:cat schemas.wire.in.social/NewSeeAlso :uuid]
                  schemas.model.social/NewSeeAlso]}
  [{:keys [definition-id definition-id-to]} author-id]
  #:see-also{:author-id author-id
             :definition-id definition-id
             :definition-id-to definition-id-to})

(defn see-also->model->wire
  {:malli/schema [:=> [:cat schemas.model.social/SeeAlso]
                  schemas.wire.out.social/SeeAlso]}
  [{:see-also/keys [see-also-id author definition-id definition-id-to created-at]}]
  (enc/assoc-some {:see-also-id see-also-id
                   :definition-id definition-id
                   :definition-id-to definition-id-to
                   :created-at created-at}
                  :author (when author (author->model->wire author))))

(defn new-note-wire->model
  {:malli/schema [:=> [:cat schemas.wire.in.social/NewNote :uuid]
                  schemas.model.social/NewNote]}
  [{:keys [definition-id body]} author-id]
  #:note{:author-id author-id
         :definition-id definition-id
         :body body})

(defn update-note-wire->model
  {:malli/schema [:=> [:cat schemas.wire.in.social/UpdateNote :uuid]
                  schemas.model.social/UpdateNote]}
  [{:keys [note-id body]} author-id]
  #:note{:note-id note-id
         :author-id author-id
         :body body})

(defn note->model->wire
  {:malli/schema [:=> [:cat schemas.model.social/Note]
                  schemas.wire.out.social/Note]}
  [{:note/keys [note-id author definition-id body created-at]}]
  (enc/assoc-some
   {:note-id note-id
    :definition-id definition-id
    :body body
    :created-at created-at}
   :author (when author (author->model->wire author))))

(defn social->model->wire
  {:malli/schema [:=> [:cat schemas.model.social/Social]
                  schemas.wire.out.social/Social]}
  [{:social/keys [definition-id notes examples see-alsos]}]
  {:definition-id definition-id
   :notes (map note->model->wire notes)
   :examples (map example->model->wire examples)
   :see-alsos (map see-also->model->wire see-alsos)})
