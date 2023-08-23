(ns integration.codes.clj.docs.backend.util
  (:require [codes.clj.docs.backend.components.db-docs :as components.db-docs]
            [codes.clj.docs.backend.server :as server]
            [com.stuartsierra.component :as component]
            [parenthesin.components.http.clj-http :as components.http]
            [parenthesin.helpers.logs :as logs]
            [parenthesin.helpers.migrations :as migrations]
            [pg-embedded-clj.core :as pg-emb]))

(defn create-and-start-components! []
  (component/start-system
   (merge (server/base-system-map)
          (component/system-map
           :http (components.http/new-http-mock {})
           :db-docs (components.db-docs/new-db-docs-mock {})))))

(defn start-system!
  ([]
   ((start-system! create-and-start-components!)))
  ([system-start-fn]
   (fn []
     (logs/setup :info :auto)
     (pg-emb/init-pg)
     (migrations/migrate (migrations/configuration-with-db))
     (system-start-fn))))

(defn stop-system!
  [system]
  (component/stop-system system)
  (pg-emb/halt-pg!))
