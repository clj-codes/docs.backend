(ns unit.codes.clj.docs.backend.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [codes.clj.docs.backend.adapters :as adapters]
            [matcher-combinators.test :refer [match?]]
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

(def db-rows
  [{:account-source "github"
    :avatar-url "https://my.pic.com/me.jpg"
    :type "note"
    :login "delboni"
    :id #uuid "d9564b50-98f8-4c04-a668-bd24c1241e34"
    :author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
    :body "my note about this function."
    :created #inst "2020-10-23T00:00:00"
    :created-at #inst "2020-10-23T00:00:00"
    :definition-id "clojure.core/disj"}
   {:account-source "github"
    :avatar-url "https://my.pic.com/me.jpg"
    :type "note"
    :login "delboni"
    :id #uuid "7aac759f-35dc-456c-9611-44589336560c"
    :author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
    :body "my second note about this function."
    :created #inst "2020-10-23T00:00:00"
    :created-at #inst "2020-10-23T00:00:00"
    :definition-id "clojure.core/disj"}
   {:account-source "github"
    :avatar-url "https://my.pic.com/me.jpg"
    :type "example"
    :login "delboni"
    :id #uuid "0f0a0fe8-7147-4d45-b212-3a32bc37d07a"
    :author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
    :body "my example about this function."
    :created #inst "2020-10-23T01:00:00"
    :created-at #inst "2020-10-23T00:00:00"
    :definition-id "clojure.core/disj"}
   {:account-source "github"
    :avatar-url "https://my.pic.com/me.jpg"
    :type "example"
    :login "delboni"
    :id #uuid "0f0a0fe8-7147-4d45-b212-3a32bc37d07a"
    :author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
    :body "my example about this function. edited"
    :created #inst "2020-10-23T02:00:00"
    :created-at #inst "2020-10-23T00:00:00"
    :definition-id "clojure.core/disj"}
   {:account-source "github"
    :avatar-url "https://my.pic.com/me.jpg"
    :type "example"
    :login "delboni"
    :id #uuid "0f0a0fe8-7147-4d45-b212-3a32bc37d07a"
    :author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
    :body "my example about this function. edited again"
    :created #inst "2020-10-23T03:00:00"
    :created-at #inst "2020-10-23T00:00:00"
    :definition-id "clojure.core/disj"}
   {:account-source "github"
    :avatar-url "https://my.pic.com/me.jpg"
    :type "example"
    :login "delboni"
    :id #uuid "c9df4a18-ec91-4d3f-9cb2-d65d48db88eb"
    :author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
    :body "another example about this function."
    :created #inst "2020-10-23T00:00:00"
    :created-at #inst "2020-10-23T00:00:00"
    :definition-id "clojure.core/disj"}
   {:account-source "github"
    :avatar-url "https://my.pic.com/me.jpg"
    :type "see-also"
    :login "delboni"
    :id #uuid "b8a824b9-6a3a-4a10-a318-58313637ecb6"
    :author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
    :body "clojure.core/dissoc"
    :created #inst "2020-10-23T00:00:00"
    :created-at #inst "2020-10-23T00:00:00"
    :definition-id "clojure.core/disj"}])

(deftest db->definitions-test
  (testing "should map and get definition social data"
    (is (match? [{:definition/definition-id "clojure.core/disj"
                  :definition/notes [#:note{:note-id #uuid "d9564b50-98f8-4c04-a668-bd24c1241e34"
                                            :definition-id "clojure.core/disj"
                                            :body "my note about this function."
                                            :created-at #inst "2020-10-23T00:00:00.000-00:00"
                                            :author #:author{:author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
                                                             :login "delboni"
                                                             :account-source "github"
                                                             :avatar-url "https://my.pic.com/me.jpg"
                                                             :created-at #inst "2020-10-23T00:00:00.000-00:00"}}
                                     #:note{:note-id #uuid "7aac759f-35dc-456c-9611-44589336560c"
                                            :definition-id "clojure.core/disj"
                                            :body "my second note about this function."
                                            :created-at #inst "2020-10-23T00:00:00.000-00:00"
                                            :author #:author{:author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
                                                             :login "delboni"
                                                             :account-source "github"
                                                             :avatar-url "https://my.pic.com/me.jpg"
                                                             :created-at
                                                             #inst "2020-10-23T00:00:00.000-00:00"}}]
                  :definition/examples [#:example{:example-id #uuid "0f0a0fe8-7147-4d45-b212-3a32bc37d07a"
                                                  :definition-id "clojure.core/disj"
                                                  :body "my example about this function. edited again"
                                                  :created-at #inst "2020-10-23T03:00:00.000-00:00"
                                                  :author #:author{:author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
                                                                   :login "delboni"
                                                                   :account-source "github"
                                                                   :avatar-url "https://my.pic.com/me.jpg"
                                                                   :created-at #inst "2020-10-23T00:00:00.000-00:00"}
                                                  :editors [#:author{:author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
                                                                     :login "delboni"
                                                                     :account-source "github"
                                                                     :avatar-url "https://my.pic.com/me.jpg"
                                                                     :created-at #inst "2020-10-23T00:00:00.000-00:00"}
                                                            #:author{:author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
                                                                     :login "delboni"
                                                                     :account-source "github"
                                                                     :avatar-url "https://my.pic.com/me.jpg"
                                                                     :created-at #inst "2020-10-23T00:00:00.000-00:00"}
                                                            #:author{:author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
                                                                     :login "delboni"
                                                                     :account-source "github"
                                                                     :avatar-url "https://my.pic.com/me.jpg"
                                                                     :created-at #inst "2020-10-23T00:00:00.000-00:00"}]}
                                        #:example{:example-id #uuid "c9df4a18-ec91-4d3f-9cb2-d65d48db88eb"
                                                  :definition-id "clojure.core/disj"
                                                  :body "another example about this function."
                                                  :created-at #inst "2020-10-23T00:00:00.000-00:00"
                                                  :author #:author{:author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
                                                                   :login "delboni"
                                                                   :account-source "github"
                                                                   :avatar-url "https://my.pic.com/me.jpg"
                                                                   :created-at #inst "2020-10-23T00:00:00.000-00:00"}
                                                  :editors [#:author{:author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
                                                                     :login "delboni"
                                                                     :account-source "github"
                                                                     :avatar-url "https://my.pic.com/me.jpg"
                                                                     :created-at #inst "2020-10-23T00:00:00.000-00:00"}]}]
                  :definition/see-alsos [#:see-also{:see-also-id #uuid "b8a824b9-6a3a-4a10-a318-58313637ecb6"
                                                    :definition-id "clojure.core/disj"
                                                    :definition-id-to "clojure.core/dissoc"
                                                    :created-at #inst "2020-10-23T00:00:00.000-00:00"
                                                    :author #:author{:author-id #uuid "387863e6-e32b-4d4b-8ec5-8cf4dab7e048"
                                                                     :login "delboni"
                                                                     :account-source "github"
                                                                     :avatar-url "https://my.pic.com/me.jpg"
                                                                     :created-at
                                                                     #inst "2020-10-23T00:00:00.000-00:00"}}]}]
                (adapters/db->definitions db-rows)))))
