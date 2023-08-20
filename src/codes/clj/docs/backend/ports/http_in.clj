(ns codes.clj.docs.backend.ports.http-in
  (:require [codes.clj.docs.backend.adapters.social :as adapters.social]
            [codes.clj.docs.backend.controllers.social :as controllers.social]))

(defn upsert-author
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body (-> author
             adapters.social/upsert-author-wire->model
             (controllers.social/upsert-author components)
             adapters.social/author->model->wire)})

(defn get-author
  [{{{:keys [login source]} :path} :parameters
    components :components}]
  (if-let [author (controllers.social/get-author login source components)]
    {:status 200
     :body (adapters.social/author->model->wire author)}
    {:status 404
     :body "not found"}))

(defn insert-see-also
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body {:id (-> author
                  adapters.social/new-see-also-wire->model
                  (controllers.social/insert-see-also components)
                  adapters.social/see-also->model->wire)}})

(defn insert-example
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body {:id (-> author
                  adapters.social/new-example-wire->model
                  (controllers.social/insert-example components)
                  adapters.social/example->model->wire)}})

(defn update-example
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body {:id (-> author
                  adapters.social/update-example-wire->model
                  (controllers.social/update-example components)
                  adapters.social/example->model->wire)}})

(defn insert-note
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body {:id (-> author
                  adapters.social/new-note-wire->model
                  (controllers.social/insert-note components)
                  adapters.social/note->model->wire)}})

(defn update-note
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body {:id (-> author
                  adapters.social/update-note-wire->model
                  (controllers.social/update-note components)
                  adapters.social/note->model->wire)}})
