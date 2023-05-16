(ns codes.clj.docs.backend.components.datalevin
  (:require [clojure.java.io :as io]
            [codes.clj.docs.backend.schemas.types :as schemas.types]
            [datalevin.util :as util]
            [parenthesin.components.http.clj-http :as http])
  (:import [java.io File]))

(defn ^:private get-db-download-url
  {:malli/schema [:=> [:cat schemas.types/GenericComponent] :string]}
  [config]
  (let [{:keys [url version file-name]} (-> config :config :db-docs)]
    (format "%s/%s/%s" url version file-name)))

(defn ^:private download-input-stream!
  {:malli/schema [:=> [:cat :string schemas.types/HttpComponent] bytes?]}
  [url http]
  (-> http
      (http/request {:url url
                     :method get
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
              saveFile (File. file-path)]
          (if (.isDirectory file-data)
            (when-not (.exists saveFile)
              (.mkdirs saveFile))
            (let [parentDir (File. (.substring file-path 0 (.lastIndexOf file-path (int File/separatorChar))))]
              (when-not (.exists parentDir)
                (.mkdirs parentDir))
              (io/copy stream saveFile))))
        (recur (.getNextEntry stream))))))

(defn download-db!
  {:malli/schema [:=> [:cat schemas.types/GenericComponent schemas.types/HttpComponent] :nil]}
  [config http]
  (let [{:keys [dir version]} (-> config :config :db-docs :dir)
        db-path (str dir File/separatorChar version)]
    (when (util/empty-dir? (File. db-path))
      (-> config
          get-db-download-url
          (download-input-stream! http)
          (unzip-stream! db-path)))))
