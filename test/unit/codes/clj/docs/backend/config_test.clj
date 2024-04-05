(ns unit.codes.clj.docs.backend.config-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [codes.clj.docs.backend.config :as backend.config]
            [com.stuartsierra.component :as component]
            [matcher-combinators.matchers :refer [embeds]]
            [matcher-combinators.test :refer [match?]]
            [parenthesin.components.config.aero :as config.aero]
            [parenthesin.helpers.malli :as helpers.malli]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defn- create-and-start-system!
  [{:keys [config]}]
  (component/start-system
   (component/system-map :config config)))

(deftest str-var->vector-var-test
  (let [system (create-and-start-system!
                {:config (config.aero/new-config {:some-config "value1, value2"
                                                  :another-config
                                                  {:nested-config "value3, value4"}
                                                  :malformed-config "value5,   value6,value7, "
                                                  :trailing-comma "value8, value9,"})})
        config-component (:config system)]

       (testing "root-level configs should be converted to vectors"
                (is (match? {:config {:some-config ["value1" "value2"]}}
                            (#'backend.config/str-var->vector-var
                             config-component [:config :some-config]))))

       (testing "nested configs should be converted to vectors"
                (is (match? {:config {:another-config {:nested-config ["value3" "value4"]}}}
                            (#'backend.config/str-var->vector-var
                             config-component [:config :another-config :nested-config]))))

       (testing "trailing and extra whitespaces should be ignored"
                (is (match? {:config {:malformed-config ["value5" "value6" "value7"]}}
                            (#'backend.config/str-var->vector-var
                             config-component [:config :malformed-config]))))

       (testing "trailing commas should be ignored"
                (is (match? {:config {:trailing-comma ["value8" "value9"]}}
                            (#'backend.config/str-var->vector-var
                             config-component [:config :trailing-comma]))))

       (let [vector-env-vars [[:config :some-config]
                              [:config :another-config :nested-config]
                              [:config :malformed-config]
                              [:config :trailing-comma]]
             no-matching-env-vars [[:config :non-existent-config]]
             converted-configs {:some-config ["value1" "value2"],
                                :another-config {:nested-config ["value3" "value4"]}
                                :malformed-config ["value5" "value6" "value7"]
                                :trailing-comma ["value8" "value9"]}
             unaltered-configs {:some-config "value1, value2",
                                :another-config {:nested-config "value3, value4"}
                                :malformed-config "value5,   value6,value7, "
                                :trailing-comma "value8, value9,"}]

            (testing "all defined vector-env-vars should be processed"
                     (is (match? {:config (embeds converted-configs)}
                                 (#'backend.config/resolved-envs-config config-component
                                                                        vector-env-vars))))

            (testing "when vector-env-vars is empty, the config should be left unaltered"
                     (is (match? {:config (embeds unaltered-configs)}
                                 (#'backend.config/resolved-envs-config config-component
                                                                        []))))

            (testing "when vector-env-vars has no matches, the config should be left unaltered"
                     (is (match? {:config (embeds unaltered-configs)}
                                 (#'backend.config/resolved-envs-config config-component
                                                                        no-matching-env-vars)))))))
