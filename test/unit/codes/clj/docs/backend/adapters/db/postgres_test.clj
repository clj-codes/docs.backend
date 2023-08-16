(ns unit.codes.clj.docs.backend.adapters.db.postgres-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [codes.clj.docs.backend.adapters.db.postgres :as adapters]
            [codes.clj.docs.backend.schemas.db.postgres :as schemas.db]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.util :as mu]
            [matcher-combinators.test :refer [match?]]))

(defspec db->author-test 50
  (properties/for-all [row (mg/generator schemas.db/UnionRow)]
                      (m/validate schemas.db/Author (adapters/db->author row))))

(defspec db->note-test 50
  (properties/for-all [row (mg/generator (mu/assoc schemas.db/UnionRow :type [:enum "note"]))]
                      (m/validate schemas.db/Note (adapters/db->note row))))

(defspec db->notes-test 50
  (properties/for-all [rows (mg/generator (mu/assoc [:sequential schemas.db/UnionRow] :type [:enum "note"]))]
                      (m/validate [:sequential schemas.db/Note] (adapters/db->notes rows))))

(defspec db->example-test 50
  (properties/for-all [row (mg/generator (mu/assoc schemas.db/UnionRow :type [:enum "example"]))
                       editors (mg/generator [:sequential schemas.db/Author])]
                      (m/validate schemas.db/Example (adapters/db->example row editors))))

(defspec db->examples-test 50
  (properties/for-all [rows (mg/generator (mu/assoc schemas.db/UnionRow :type [:enum "example"]))]
                      (m/validate [:sequential schemas.db/Example] (adapters/db->examples rows))))

(defspec db->see-also-test 50
  (properties/for-all [row (mg/generator (mu/assoc schemas.db/UnionRow :type [:enum "see-also"]))]
                      (m/validate schemas.db/SeeAlso (adapters/db->see-also row))))

(defspec db->see-alsos-test 50
  (properties/for-all [rows (mg/generator (mu/assoc schemas.db/UnionRow :type [:enum "see-also"]))]
                      (m/validate [:sequential schemas.db/SeeAlso] (adapters/db->see-alsos rows))))

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
