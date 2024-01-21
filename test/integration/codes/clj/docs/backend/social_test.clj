(ns integration.codes.clj.docs.backend.social-test
  (:require [clojure.test :refer [use-fixtures]]
            [integration.codes.clj.docs.backend.util :as util]
            [integration.codes.clj.docs.backend.util.db.postgres :as util.db.postgres]
            [parenthesin.helpers.malli :as helpers.malli]
            [parenthesin.helpers.state-flow.server.pedestal :as state-flow.server]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defflow
  flow-integration-author-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "should create author"
      (match? {:status 201
               :body  {:author-id string?
                       :login "delboni"
                       :account-source "github"
                       :avatar-url "https://my.pic/me.jpg"
                       :created-at string?}}
              (state-flow.server/request! {:method :post
                                           :uri    "/social/author/"
                                           :body   {:login "delboni"
                                                    :account-source "github"
                                                    :avatar-url "https://my.pic/me.jpg"}})))

    (flow "should return author"
      (match? {:status 200
               :body {:author-id string?
                      :login "delboni",
                      :account-source "github",
                      :avatar-url "https://my.pic/me.jpg",
                      :created-at string?}}
              (state-flow.server/request! {:method :get
                                           :uri    "/social/author/delboni/github"})))))

(defflow
  flow-integration-note-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    [author-response (state-flow.server/request! {:method :post
                                                  :uri    "/social/author/"
                                                  :body   {:login "delboni"
                                                           :account-source "github"
                                                           :avatar-url "https://my.pic/me.jpg"}})
     :let [author-id (-> author-response :body :author-id)]]

    (flow "create & update note"
      [new-note-response (state-flow.server/request! {:method :post
                                                      :uri    "/social/note/"
                                                      :body   {:author-id author-id
                                                               :definition-id "clojure.core/disj"
                                                               :body "my note about this function."}})
       :let [note-id (-> new-note-response :body :note-id)]]

      (flow "check new note response"
        (match? {:status 201
                 :body {:note-id string?
                        :definition-id "clojure.core/disj",
                        :body "my note about this function.",
                        :created-at string?}}
                new-note-response)

        (flow "checks db for new note"
          (match? {:status 200
                   :body {:definition-id "clojure.core/disj"
                          :notes [{:note-id note-id
                                   :definition-id "clojure.core/disj"
                                   :body "my note about this function."
                                   :created-at string?}]}}
                  (state-flow.server/request! {:method :get
                                               :uri "/social/definition/clojure.core/disj"}))))

      (flow "check update note response"
        (match? {:status 201
                 :body {:note-id note-id
                        :definition-id "clojure.core/disj"
                        :body "my edited note about this function."
                        :created-at string?}}
                (state-flow.server/request! {:method :put
                                             :uri    "/social/note/"
                                             :body   {:author-id author-id
                                                      :note-id note-id
                                                      :definition-id "clojure.core/disj"
                                                      :body "my edited note about this function."}}))

        (flow "checks db for updated note"
          (match? {:status 200
                   :body {:definition-id "clojure.core/disj"
                          :notes [{:note-id note-id
                                   :definition-id "clojure.core/disj"
                                   :body "my edited note about this function."
                                   :created-at string?}]}}
                  (state-flow.server/request! {:method :get
                                               :uri "/social/definition/clojure.core/disj"})))))))

(defflow
  flow-integration-see-also-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    [author-response (state-flow.server/request! {:method :post
                                                  :uri    "/social/author/"
                                                  :body   {:login "delboni"
                                                           :account-source "github"
                                                           :avatar-url "https://my.pic/me.jpg"}})
     :let [author-id (-> author-response :body :author-id)]]

    (flow "create & update see-also"
      [new-see-also-response (state-flow.server/request! {:method :post
                                                          :uri    "/social/see-also/"
                                                          :body   {:author-id author-id
                                                                   :definition-id "clojure.core/disj"
                                                                   :definition-id-to "clojure.core/dissoc"}})
       :let [see-also-id (-> new-see-also-response :body :see-also-id)]]

      (flow "check new see-also response"
        (match? {:status 201
                 :body {:see-also-id string?
                        :definition-id "clojure.core/disj"
                        :definition-id-to "clojure.core/dissoc"
                        :created-at string?}}
                new-see-also-response)

        (flow "checks db for new see-also"
          (match? {:status 200
                   :body {:definition-id "clojure.core/disj"
                          :see-alsos [{:see-also-id see-also-id
                                       :definition-id "clojure.core/disj"
                                       :definition-id-to "clojure.core/dissoc"
                                       :created-at string?}]}}
                  (state-flow.server/request! {:method :get
                                               :uri "/social/definition/clojure.core/disj"})))))))

(defflow
  flow-integration-example-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    [author-response (state-flow.server/request! {:method :post
                                                  :uri    "/social/author/"
                                                  :body   {:login "delboni"
                                                           :account-source "github"
                                                           :avatar-url "https://my.pic/me.jpg"}})
     :let [author-id (-> author-response :body :author-id)]]

    (flow "create & update example"
      [new-example-response (state-flow.server/request! {:method :post
                                                         :uri    "/social/example/"
                                                         :body   {:author-id author-id
                                                                  :definition-id "clojure.core/disj"
                                                                  :body "my example about this function."}})
       :let [example-id (-> new-example-response :body :example-id)]]

      (flow "check new example response"
        (match? {:status 201
                 :body {:example-id string?
                        :definition-id "clojure.core/disj",
                        :body "my example about this function.",
                        :created-at string?}}
                new-example-response)

        (flow "checks db for new example"
          (match? {:status 200
                   :body {:definition-id "clojure.core/disj"
                          :examples [{:example-id example-id
                                      :definition-id "clojure.core/disj"
                                      :body "my example about this function."
                                      :created-at string?}]}}
                  (state-flow.server/request! {:method :get
                                               :uri "/social/definition/clojure.core/disj"}))))

      (flow "check update example response"
        (match? {:status 201
                 :body {:example-id example-id
                        :definition-id "clojure.core/disj"
                        :body "my edited example about this function."
                        :created-at string?}}
                (state-flow.server/request! {:method :put
                                             :uri    "/social/example/"
                                             :body   {:author-id author-id
                                                      :example-id example-id
                                                      :definition-id "clojure.core/disj"
                                                      :body "my edited example about this function."}}))

        (flow "checks db for updated example"
          (match? {:status 200
                   :body {:definition-id "clojure.core/disj"
                          :examples [{:example-id example-id
                                      :definition-id "clojure.core/disj"
                                      :body "my edited example about this function."
                                      :created-at string?}]}}
                  (state-flow.server/request! {:method :get
                                               :uri "/social/definition/clojure.core/disj"})))))))
