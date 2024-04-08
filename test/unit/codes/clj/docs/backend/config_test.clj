(ns unit.codes.clj.docs.backend.config-test
  (:require [aero.core :as aero]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [codes.clj.docs.backend.config :as backend.config]
            [matcher-combinators.test :refer [match?]]
            [parenthesin.helpers.malli :as helpers.malli]))

(use-fixtures :once helpers.malli/with-intrumentation)

(deftest str-var->vector-var-test
  (testing "csv configs should be converted to vectors"
    (is (match? ["value1" "value2"]
                (#'backend.config/str-var->vector-var "value1, value2"))))

  (testing "trailing and extra whitespaces should be ignored"
    (is (match? ["value3" "value4" "value5"]
                (#'backend.config/str-var->vector-var "value3,   value4,value5, "))))

  (testing "trailing commas should be ignored"
    (is (match? ["value6" "value7"]
                (#'backend.config/str-var->vector-var "value6, value7,")))))

(deftest csv-reader-test
  (testing "tag literal #csv should turn comma-separated strings into vectors"
    (is (match? {:some-config ["value1" "value2"]
                 :malformed-config ["value3" "value4" "value5"]
                 :trailing-comma-config ["value6" "value7"]}
                (aero/read-config "test/resources/csv-config.edn")))))
