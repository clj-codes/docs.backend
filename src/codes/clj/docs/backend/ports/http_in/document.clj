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
    (let [project (-> namespaces first :namespace/project)]
      {:status 200
       :body {:project (adapters.document/project->wire project)
              :namespaces (adapters.document/namespaces->wire namespaces)}})
    {:status 404
     :body "not found"}))

(defn get-definitions-by-namespace
  [{{{:keys [namespace-id]} :path} :parameters
    components :components}]
  (if-let [definitions (-> namespace-id
                           (controllers.document/get-definitions-by-namespace components)
                           seq)]
    (let [namespace (-> definitions first :definition/namespace)
          project (:namespace/project namespace)]
      {:status 200
       :body {:project (adapters.document/project->wire project)
              :namespace (adapters.document/namespace->wire namespace)
              :definitions (adapters.document/definitions->wire definitions)}})
    {:status 404
     :body "not found"}))

(defn get-definition-by-id
  [{{{:keys [definition-id]} :path} :parameters
    components :components}]
  (if-let [definition (controllers.document/get-definition-by-id definition-id components)]
    (let [namespace (:definition/namespace definition)
          project (:namespace/project namespace)]
      {:status 200
       :body {:project (adapters.document/project->wire project)
              :namespace (adapters.document/namespace->wire namespace)
              :definition (adapters.document/definition->wire definition)}})
    {:status 404
     :body "not found"}))

(defn search-by-fulltext
  [{{{:keys [q top]} :query} :parameters
    components :components}]
  {:status 200
   :body (-> (controllers.document/search-by-fulltext q (or top 20) components)
             adapters.document/search-results->wire)})
