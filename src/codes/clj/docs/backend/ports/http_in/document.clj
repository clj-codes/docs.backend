(ns codes.clj.docs.backend.ports.http-in.document
  (:require [codes.clj.docs.backend.adapters.document :as adapters.document]
            [codes.clj.docs.backend.controllers.document :as controllers.document]))

(defn get-projects
  [{components :components}]
  {:status 200
   :body (-> (controllers.document/get-projects components)
             adapters.document/projects->wire)})

(defn get-namespaces-by-project
  [{{{:keys [project-id]} :path} :parameters
    components :components}]
  (if-let [namespaces (-> project-id
                          (controllers.document/get-namespaces-by-project components)
                          seq)]
    {:status 200
     :body (adapters.document/namespaces->wire namespaces)}
    {:status 404
     :body "not found"}))

(defn get-definitions-by-namespace
  [{{{:keys [namespace-id]} :path} :parameters
    components :components}]
  (if-let [definitions (-> namespace-id
                           (controllers.document/get-definitions-by-namespace components)
                           seq)]
    {:status 200
     :body (adapters.document/definitions->wire definitions)}
    {:status 404
     :body "not found"}))
