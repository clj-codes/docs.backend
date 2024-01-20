(ns codes.clj.docs.backend.components.db-docs
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [datalevin.core :as d]
            [datalevin.util :as util]
            [malli.core :as m]
            [parenthesin.components.http.clj-http :as http]
            [parenthesin.helpers.logs :as logs])
  (:import [java.io File]
           [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(def GenericComponent
  (m/-simple-schema
   {:type :generic-component
    :pred #(satisfies? component/Lifecycle %)
    :type-properties {:error/message "should satisfy com.stuartsierra.component/Lifecycle protocol."}}))

(defn ^:private get-db-download-url
  {:malli/schema [:=> [:cat GenericComponent] :string]}
  [config]
  (let [{:keys [url version file-name]} (-> config :config :db-docs)]
    (format "%s/%s/%s" url version file-name)))

(defn ^:private download-input-stream!
  {:malli/schema [:=> [:cat :string GenericComponent] bytes?]}
  [url http]
  (-> http
      (http/request {:url url
                     :method :get
                     :as :byte-array})
      :body
      io/input-stream))

(defn ^:private unzip-stream!
  {:malli/schema [:=> [:cat bytes? :string] :nil]}
  [input-stream dir]
  (let [stream (java.util.zip.ZipInputStream. input-stream)]
    (loop [file-data (.getNextEntry stream)]
      (when file-data
        (let [file-path (str dir File/separatorChar (.getName file-data))
              saveFile (io/file file-path)]
          (if (.isDirectory file-data)
            (when-not (.exists saveFile)
              (.mkdirs saveFile))
            (let [parentDir (io/file (.substring file-path 0 (.lastIndexOf file-path (int File/separatorChar))))]
              (when-not (.exists parentDir)
                (.mkdirs parentDir))
              (io/copy stream saveFile))))
        (recur (.getNextEntry stream))))))

(defn download-db!
  {:malli/schema [:=> [:cat :string GenericComponent GenericComponent] :nil]}
  [db-path config http]
  (-> config
      get-db-download-url
      (download-input-stream! http)
      (unzip-stream! db-path)))

(defprotocol DbDocsProvider
  (db [component]
    "Returns a database snapshot")
  (conn [component]
    "Returns a database connection"))

(defrecord DbDocs [schema config http conn]
  component/Lifecycle
  (start [this]
    (logs/log :info :datalevin :start)
    (let [{:keys [dir version]} (-> config :config :db-docs)
          db-path (str dir File/separatorChar version)]
      (when-not (util/file-exists db-path)
        (logs/log :info :datalevin :db-not-found :downloading :start)
        (download-db! db-path config http)
        (logs/log :info :datalevin :db-not-found :downloading :end)
        (if conn
          this
          (assoc this :conn (d/get-conn db-path schema))))))
  (stop [this]
    (logs/log :info :datalevin :stop)
    (if conn
      (do
        (d/close conn)
        (assoc this :conn nil))
      this))

  DbDocsProvider
  (db [this]
    (d/db (:conn this)))

  (conn [this]
    (:conn this)))

(defn new-db-docs [schema]
  (map->DbDocs {:schema schema}))

(defrecord DbDocsMock [schema conn db-path]
  component/Lifecycle
  (start [this]
    (logs/log :info :datalevin :start)
    (let [db-path (str (Files/createTempDirectory
                        "db-docs"
                        (into-array FileAttribute [])))]
      (if conn
        this
        (assoc this
               :conn (d/get-conn db-path schema)
               :db-path db-path))))
  (stop [this]
    (logs/log :info :datalevin :stop)
    (if conn
      (do
        (d/close conn)
        (util/delete-files db-path)
        (assoc this :conn nil))
      this))

  DbDocsProvider
  (db [this]
    (d/db (:conn this)))

  (conn [this]
    (:conn this)))

(defn new-db-docs-mock [schema]
  (map->DbDocsMock {:schema schema}))
