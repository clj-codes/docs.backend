(ns codes.clj.docs.backend.ports.http-in
  (:require [codes.clj.docs.backend.adapters.social :as adapters.social]
            [codes.clj.docs.backend.controllers.social :as controllers.social]))

(defn upsert-author
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body {:id (-> author
                  adapters.social/upsert-author-wire->model
                  (controllers.social/upsert-author components)
                  :author/author-id)}})

(defn get-author
  [{{{:keys [login source]} :path} :parameters
    components :components}]
  (if-let [author (controllers.social/get-author login (keyword source) components)]
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
                  :see-also-id)}})

(defn insert-example
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body {:id (-> author
                  adapters.social/new-example-wire->model
                  (controllers.social/insert-example components)
                  :example-id)}})

(defn update-example
  [{{author :body} :parameters
    components :components}]
  {:status 201
   :body {:id (-> author
                  adapters.social/update-example-wire->model
                  (controllers.social/update-example components)
                  :example-id)}})
