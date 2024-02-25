(ns integration.codes.clj.docs.backend.util
  (:require [codes.clj.docs.backend.components.db-docs :as components.db-docs]
            [codes.clj.docs.backend.db.datalevin :refer [read-conn-opts]]
            [codes.clj.docs.backend.server :as server]
            [com.stuartsierra.component :as component]
            [datalevin.search-utils :as su]
            [parenthesin.components.http.clj-http :as components.http]
            [parenthesin.helpers.logs :as logs]
            [parenthesin.helpers.migrations :as migrations]
            [pg-embedded-clj.core :as pg-emb]))

(def minimal-schema
  {:project/id          {:db/valueType :db.type/string
                         :unique :db.unique/identity}
   :project/group       {:db/valueType :db.type/string
                         :db/fulltext  true
                         :db.fulltext/domains ["project"
                                               "project-group"]}
   :project/artifact    {:db/valueType :db.type/string
                         :db/fulltext  true
                         :db.fulltext/domains ["project"
                                               "project-name"]}
   :namespace/id        {:db/valueType :db.type/string
                         :unique :db.unique/identity}
   :namespace/project   {:db/valueType :db.type/ref}
   :namespace/name      {:db/valueType :db.type/string
                         :db/fulltext  true
                         :db.fulltext/domains ["namespace"
                                               "namespace-name"]}
   :namespace/doc       {:db/valueType :db.type/string
                         :db/fulltext  true
                         :db.fulltext/autoDomain true
                         :db.fulltext/domains ["namespace"
                                               "namespace-doc"]}
   :definition/id        {:db/valueType :db.type/string
                          :unique :db.unique/identity}
   :definition/namespace {:db/valueType :db.type/ref}
   :definition/name      {:db/valueType :db.type/string
                          :db/fulltext  true
                          :db.fulltext/domains ["definition"
                                                "definition-name"]}
   :definition/doc       {:db/valueType :db.type/string
                          :db/fulltext  true
                          :db.fulltext/domains ["definition"
                                                "definition-doc"]}})

(def write-conn-opts
  (let [analyzer (su/create-analyzer
                  {:tokenizer (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+")
                   :token-filters [su/lower-case-token-filter
                                   su/prefix-token-filter]})]
    (-> read-conn-opts
        (assoc-in [:search-domains "project-name" :analyzer] analyzer)
        (assoc-in [:search-domains "namespace-name" :analyzer] analyzer)
        (assoc-in [:search-domains "definition-name" :analyzer] analyzer))))

(defn create-and-start-components! []
  (component/start-system
   (merge (server/base-system-map)
          (component/system-map
           :http (components.http/new-http-mock {})
           :db-docs (components.db-docs/new-db-docs-mock minimal-schema write-conn-opts)))))

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
