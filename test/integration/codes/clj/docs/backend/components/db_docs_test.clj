(ns integration.codes.clj.docs.backend.components.db-docs-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [codes.clj.docs.backend.components.db-docs :as component.db-docs]
            [com.stuartsierra.component :as component]
            [datalevin.core :as d]
            [datalevin.util :as util]
            [matcher-combinators.matchers :as m]
            [matcher-combinators.test :refer [match?]]
            [parenthesin.components.config.aero :as component.config]
            [parenthesin.components.http.clj-http :as component.http]
            [parenthesin.helpers.malli :as helpers.malli]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defn- create-and-start-system! [components]
  (->> components
       (reduce (fn [acc [k v]] (into acc [k v])) [])
       (apply component/system-map)
       component/start-system))

(deftest db-docs-component-test
  (testing "db-docs should download unzip and start db"
    (let [db-path "target/tmp/v0.0.1"
          _delete (when (util/file-exists db-path)
                    (util/delete-files db-path))
          system (create-and-start-system!
                  {:config (component.config/new-config
                            {:db-docs {:dir "target/tmp"
                                       :url "https://my-url"
                                       :version "v0.0.1"
                                       :file-name "db.zip"}})
                   :http (component.http/new-http-mock
                          {"https://my-url/v0.0.1/db.zip"
                           {:status 200
                            :body (io/input-stream
                                   (io/resource "docs-db.zip"))}})
                   :db-docs (component/using
                             (component.db-docs/new-db-docs {})
                             [:config :http])})]

      (is (match? (m/in-any-order
                   ["target/tmp/v0.0.1"
                    "target/tmp/v0.0.1/lock.mdb"
                    "target/tmp/v0.0.1/test.file"
                    "target/tmp/v0.0.1/data.mdb"])
                  (-> db-path
                      io/file
                      file-seq
                      (->> (mapv str)))))

      (component/stop-system system))))

(deftest db-docs-component-mock-test
  (testing "db-docs mocks should work as normal empty datalevin"
    (let [system (create-and-start-system!
                  {:db-docs (component.db-docs/new-db-docs-mock {})})]

      (d/transact! (component.db-docs/conn (:db-docs system))
                   [{:name "Frege" :db/id -1 :nation "France" :aka ["foo" "fred"]}
                    {:name "Peirce" :db/id -2 :nation "france"}
                    {:name "De Morgan" :db/id -3 :nation "English"}])

      (is (match? {:db/id 3, :name "De Morgan", :nation "English"}
                  (d/q '[:find (pull ?e [*]) .
                         :in $ ?nation
                         :where
                         [?e :nation ?nation]]
                       (component.db-docs/db (:db-docs system))
                       "English")))

      (component/stop-system system))))
