(ns codes.clj.docs.backend.ports.http-in.social
  (:require [codes.clj.docs.backend.adapters.social :as adapters.social]
            [codes.clj.docs.backend.controllers.social :as controllers.social]
            [codes.clj.docs.backend.controllers.social.github :as controllers.social.github]
            [codes.clj.docs.backend.ports.jwt :as ports.jwt]))

(defn author-login-github
  [{{github :body} :parameters
    components :components}]
  (let [author (-> (:code github)
                   (controllers.social.github/get-author components)
                   (controllers.social/upsert-author components)
                   adapters.social/author->model->wire)
        access-token (ports.jwt/encrypt author (:config components))]
    {:status 201
     :body {:author author
            :access-token access-token}}))

(defn get-author
  [{{{:keys [login source]} :path} :parameters
    components :components}]
  (if-let [author (controllers.social/get-author login source components)]
    {:status 200
     :body (adapters.social/author->model->wire author)}
    {:status 404
     :body "not found"}))

(defn insert-see-also
  [{{see-also :body} :parameters
    components :components
    auth :auth}]
  {:status 201
   :body (-> see-also
             (adapters.social/new-see-also-wire->model (:author-id auth))
             (controllers.social/insert-see-also components)
             adapters.social/see-also->model->wire)})

;; TODO delete-see-also, routes & integration tests
(defn delete-see-also
  [{{{:keys [see-also-id]} :path} :parameters
    components :components
    auth :auth}]
  (let [{author :note/author} (controllers.social/get-see-also see-also-id components)]
    (if (= (:author/author-id author) (:author-id auth))
      {:status 202
       :body {:see-also-id (controllers.social/delete-see-also see-also-id components)}}
      {:status 403
       :body "You not allowed to delete this see also."})))

(defn get-see-also
  [{{{:keys [see-also-id]} :path} :parameters
    components :components}]
  (if-let [see-also (controllers.social/get-see-also see-also-id components)]
    {:status 200
     :body (adapters.social/see-also->model->wire see-also)}
    {:status 404
     :body "Not found."}))

(defn insert-example
  [{{example :body} :parameters
    components :components
    auth :auth}]
  {:status 201
   :body (-> example
             (adapters.social/new-example-wire->model (:author-id auth))
             (controllers.social/insert-example components)
             adapters.social/example->model->wire)})

(defn update-example
  [{{example :body} :parameters
    components :components
    auth :auth}]
  {:status 201
   :body (-> example
             (adapters.social/update-example-wire->model (:author-id auth))
             (controllers.social/update-example components)
             adapters.social/example->model->wire)})

(defn get-example
  [{{{:keys [example-id]} :path} :parameters
    components :components}]
  (if-let [example (controllers.social/get-example example-id components)]
    {:status 200
     :body (adapters.social/example->model->wire example)}
    {:status 404
     :body "Not found."}))

(defn insert-note
  [{{note :body} :parameters
    components :components
    auth :auth}]
  {:status 201
   :body (-> note
             (adapters.social/new-note-wire->model (:author-id auth))
             (controllers.social/insert-note components)
             adapters.social/note->model->wire)})

(defn update-note
  [{{note :body} :parameters
    components :components
    auth :auth}]
  (let [{author :note/author} (controllers.social/get-note (:note-id note) components)]
    (if (= (:author/author-id author) (:author-id auth))
      {:status 201
       :body (-> note
                 (adapters.social/update-note-wire->model (:author-id auth))
                 (controllers.social/update-note components)
                 adapters.social/note->model->wire)}
      {:status 403
       :body "You not allowed to update this note."})))

;; TODO delete-note, routes & integration tests
(defn delete-note
  [{{{:keys [note-id]} :path} :parameters
    components :components
    auth :auth}]
  (let [{author :note/author} (controllers.social/get-note note-id components)]
    (if (= (:author/author-id author) (:author-id auth))
      {:status 202
       :body {:note-id (controllers.social/delete-note note-id components)}}
      {:status 403
       :body "You not allowed to delete this see also."})))

(defn get-note
  [{{{:keys [note-id]} :path} :parameters
    components :components}]
  (if-let [note (controllers.social/get-note note-id components)]
    {:status 200
     :body (adapters.social/note->model->wire note)}
    {:status 404
     :body "Not found."}))

(defn get-by-definition
  [{{{:keys [definition-id]} :path} :parameters
    components :components}]
  {:status 200
   :body (if-let [definition (controllers.social/get-by-definition definition-id components)]
           (adapters.social/social->model->wire definition)
           {:definition-id definition-id
            :notes []
            :examples []
            :see-alsos []})})
