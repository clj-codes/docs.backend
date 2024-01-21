(ns codes.clj.docs.backend.adapters.social
  (:require [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.wire.in.social :as schemas.wire.in.social]
            [codes.clj.docs.backend.schemas.wire.out.social :as schemas.wire.out.social]
            [codes.clj.docs.backend.schemas.wire.social :as schemas.wire.social]
            [taoensso.encore :as enc]))

(defn upsert-author-wire->model
  {:malli/schema [:=> [:cat schemas.wire.in.social/NewAuthor]
                  schemas.model.social/NewAuthor]}
  [{:keys [login account-source avatar-url]}]
  #:author{:login login
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
  {:malli/schema [:=> [:cat schemas.wire.in.social/NewExample]
                  schemas.model.social/NewExample]}
  [{:keys [author-id definition-id body]}]
  #:example{:author-id author-id
            :definition-id definition-id
            :body body})

(defn update-example-wire->model
  {:malli/schema [:=> [:cat schemas.wire.in.social/UpdateExample]
                  schemas.model.social/UpdateExample]}
  [{:keys [example-id author-id body]}]
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
  {:malli/schema [:=> [:cat schemas.wire.in.social/NewSeeAlso]
                  schemas.model.social/NewSeeAlso]}
  [{:keys [author-id definition-id definition-id-to]}]
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
  {:malli/schema [:=> [:cat schemas.wire.in.social/NewNote]
                  schemas.model.social/NewNote]}
  [{:keys [author-id definition-id body]}]
  #:note{:author-id author-id
         :definition-id definition-id
         :body body})

(defn update-note-wire->model
  {:malli/schema [:=> [:cat schemas.wire.in.social/UpdateNote]
                  schemas.model.social/UpdateNote]}
  [{:keys [note-id author-id definition-id body]}]
  #:note{:note-id note-id
         :author-id author-id
         :definition-id definition-id
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

(defn definition->model->wire
  {:malli/schema [:=> [:cat schemas.model.social/Definition]
                  schemas.wire.out.social/Definition]}
  [{:definition/keys [definition-id notes examples see-alsos]}]
  {:definition-id definition-id
   :notes (map note->model->wire notes)
   :examples (map example->model->wire examples)
   :see-alsos (map see-also->model->wire see-alsos)})
