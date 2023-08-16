(ns unit.codes.clj.docs.backend.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [codes.clj.docs.backend.adapters :as adapters]
            [parenthesin.helpers.malli :as helpers.malli]))

(use-fixtures :once helpers.malli/with-intrumentation)

(deftest inst->formated-string
  (testing "should adapt clojure/instant to formated string"
    (is (= "1987-02-10 09:38:43"
           (adapters/inst->utc-formated-string #inst "1987-02-10T09:38:43.000Z"
                                               "yyyy-MM-dd hh:mm:ss")))))

(def coindesk-response-fixture
  {:time {:updated "Jun 26, 2021 20:06:00 UTC"
          :updatedISO "2021-06-26T20:06:00+00:00"
          :updateduk "Jun 26, 2021 at 21:06 BST"}
   :bpi {:USD
         {:code "USD"
          :symbol "&#36;"
          :rate "31,343.9261"
          :description "United States Dollar"
          :rate_float 31343.9261}
         :GBP
         {:code "GBP"
          :symbol "&pound;"
          :rate "22,573.9582"
          :description "British Pound Sterling"
          :rate_float 22573.9582}}})

;(deftest wire->usd-price-test
  ;(testing "should adapt coindesk response into a number"
    ;(is (match? 31343.9261M
                ;(adapters/wire->usd-price coindesk-response-fixture)))))

;(defspec wire-in-db-test 50
  ;(properties/for-all [id (mg/generator :uuid)
                       ;pos-num (mg/generator [:double {:min 1 :max 999999}])
                       ;neg-num (mg/generator [:double {:min -9999 :max -1}])]
                      ;(m/validate schemas.db/WalletTransaction (adapters/withdrawal->db id neg-num pos-num))
                      ;(m/validate schemas.db/WalletTransaction (adapters/deposit->db id pos-num pos-num))))

;(defspec db-wire-in-test 50
  ;(properties/for-all [wallet-db (mg/generator schemas.db/WalletEntry {:gen/infinite? false})]
                      ;(m/validate schemas.wire-in/WalletEntry (adapters/db->wire-in wallet-db))))
