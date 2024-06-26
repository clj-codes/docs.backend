(ns codes.clj.docs.backend.server
  (:require [codes.clj.docs.backend.components.db-docs :as components.db-docs]
            [codes.clj.docs.backend.config]
            [codes.clj.docs.backend.db.datalevin :refer [read-conn-opts]]
            [codes.clj.docs.backend.routes :as routes]
            [com.stuartsierra.component :as component]
            [parenthesin.components.config.aero :as config]
            [parenthesin.components.db.jdbc-hikari :as database]
            [parenthesin.components.http.clj-http :as http]
            [parenthesin.components.router.reitit-malli :as router]
            [parenthesin.components.server.reitit-pedestal-jetty :as webserver]
            [parenthesin.helpers.logs :as logs]
            [parenthesin.helpers.migrations :as migrations])
  (:gen-class))

(def system-atom (atom nil))

(defn base-system-map []
  (component/system-map
   :config (config/new-config)
   :http (http/new-http)
   :router (router/new-router routes/routes)
   :database (component/using (database/new-database) [:config])
   :db-docs (component/using (components.db-docs/new-db-docs {} read-conn-opts) [:config :http])
   :webserver (component/using (webserver/new-webserver) [:config :http :router :database :db-docs])))

(defn start-system! [system-map]
  (logs/setup :info :auto)
  (migrations/migrate (migrations/configuration-with-db))
  (->> system-map
       component/start
       (reset! system-atom)))

#_{:clj-kondo/ignore [:unused-public-var]}
(defn stop-system! []
  (swap!
   system-atom
   (fn [s] (when s (component/stop s)))))

(defn -main
  "The entry-point for 'gen-class'"
  [& _args]
  (start-system! (base-system-map)))

(comment
  (start-system! (base-system-map))
  (stop-system!))
