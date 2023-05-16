(ns integration.codes.clj.docs.backend.components.db-docs-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [codes.clj.docs.backend.components.db-docs :as components.db-docs]
            [com.stuartsierra.component :as component]
            [datalevin.util :as util]
            [matcher-combinators.test :refer [match?]]
            [parenthesin.components.config.aero :as config.aero]
            [parenthesin.components.http.clj-http :as http.clj-http]))

(defn- create-and-start-system! [components]
  (->> components
       (reduce (fn [acc [k v]] (into acc [k v])) [])
       (apply component/system-map)
       component/start-system))

(deftest config-mock-component-test
  (testing "config should return mocked config"
    (let [db-path "target/tmp/v0.0.1"
          system (create-and-start-system!
                  {:config (config.aero/new-config
                            {:db-docs {:dir "target/tmp"
                                       :url "https://my-url"
                                       :version "v0.0.1"
                                       :file-name "db.zip"}})
                   :http (http.clj-http/new-http-mock
                          {"https://my-url/v0.0.1/db.zip"
                           {:status 200
                            :body (io/input-stream
                                   (io/resource "docs-db.zip"))}})})]

      (when (util/file-exists db-path)
        (util/delete-files db-path))

      (components.db-docs/download-db! (:config system) (:http system))

      (is (match? ["target/tmp/v0.0.1"
                   "target/tmp/v0.0.1/docs-db"
                   "target/tmp/v0.0.1/docs-db/lock.mdb"
                   "target/tmp/v0.0.1/docs-db/data.mdb"]
                  (-> db-path
                      io/file
                      file-seq
                      (->> (mapv str))))))))
