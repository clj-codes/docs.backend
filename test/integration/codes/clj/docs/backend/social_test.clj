(ns integration.codes.clj.docs.backend.social-test
  (:require [clojure.test :refer [use-fixtures]]
            [codes.clj.docs.backend.ports.jwt :as ports.jwt]
            [integration.codes.clj.docs.backend.util :as util]
            [parenthesin.helpers.malli :as helpers.malli]
            [parenthesin.helpers.state-flow.http :as state-flow.http]
            [parenthesin.helpers.state-flow.server.pedestal :as state-flow.server]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(use-fixtures :once helpers.malli/with-intrumentation)

(def github-api-mocks
  {"https://github.com/login/oauth/access_token" {:body {:access_token "gho_123456"}
                                                  :status 200}
   "https://api.github.com/user" {:body {:login "delboni"
                                         :avatar_url "https://my.pic/me.jpg"}
                                  :status 200}})

(defn create-token [login config]
  (ports.jwt/encrypt {:author-id (-> login .getBytes java.util.UUID/nameUUIDFromBytes)
                      :login login
                      :account-source "github"
                      :avatar-url "https://my.pic/me.jpg"
                      :created-at #inst "2024-03-09"}
                     config))

(defn token? [config]
  (fn [input]
    (and (try (ports.jwt/decrypt input config)
              (catch Exception _ false))
         (string? input))))

(defflow
  flow-integration-author-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    (state-flow.http/set-http-out-responses! github-api-mocks)
    (flow "should create author"
      [config (state-flow.api/get-state :config)]
      (match? {:status 201
               :body  {:author {:author-id string?
                                :login "delboni"
                                :account-source "github"
                                :avatar-url "https://my.pic/me.jpg"
                                :created-at string?}
                       :access-token (token? config)}}
              (state-flow.server/request! {:method :post
                                           :uri    "/api/login/github"
                                           :body   {:code "agc622abb6135be5d1f2"}})))

    (flow "should return author"
      (match? {:status 200
               :body {:author-id string?
                      :login "delboni",
                      :account-source "github",
                      :avatar-url "https://my.pic/me.jpg",
                      :created-at string?}}
              (state-flow.server/request! {:method :get
                                           :uri    "/api/social/author/delboni/github"})))))

(defflow
  flow-integration-note-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    (state-flow.http/set-http-out-responses! github-api-mocks)
    [config (state-flow.api/get-state :config)
     author-response (state-flow.server/request! {:method :post
                                                  :uri    "/api/login/github"
                                                  :body   {:code "agc622abb6135be5d1f2"}})
     :let [token (->> author-response :body :access-token)
           fake-token (create-token "fake-delboni" config)]]

    (flow "create & update note"
      [new-note-response (state-flow.server/request! {:method :post
                                                      :headers {"authorization" (str "Bearer " token)}
                                                      :uri    "/api/social/note/"
                                                      :body   {:definition-id "clojure.core/disj"
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
                                               :uri "/api/social/definition/clojure.core/disj"}))))

      (flow "should not be able to update if not allowed"
        (match? {:status 403
                 :body "You not allowed to update this note."}
                (state-flow.server/request! {:method :put
                                             :headers {"authorization" (str "Bearer " fake-token)}
                                             :uri    "/api/social/note/"
                                             :body   {:note-id note-id
                                                      :body "my edited note about this function."}})))

      (flow "update and check note response"
        (match? {:status 201
                 :body {:note-id note-id
                        :definition-id "clojure.core/disj"
                        :body "my edited note about this function."
                        :created-at string?}}
                (state-flow.server/request! {:method :put
                                             :headers {"authorization" (str "Bearer " token)}
                                             :uri    "/api/social/note/"
                                             :body   {:note-id note-id
                                                      :body "my edited note about this function."}})))

      (flow "checks db for updated note"
        (match? {:status 200
                 :body {:definition-id "clojure.core/disj"
                        :notes [{:note-id note-id
                                 :definition-id "clojure.core/disj"
                                 :body "my edited note about this function."
                                 :created-at string?}]}}
                (state-flow.server/request! {:method :get
                                             :uri "/api/social/definition/clojure.core/disj"})))

      (flow "checks db for updated note using get-note api"
        (match? {:status 200
                 :body {:note-id note-id
                        :definition-id "clojure.core/disj"
                        :body "my edited note about this function."
                        :created-at string?}}
                (state-flow.server/request! {:method :get
                                             :uri (str "/api/social/note/" note-id)})))

      (flow "should not be able to delete if not allowed"
        (match? {:status 403
                 :body "You not allowed to delete this note."}
                (state-flow.server/request! {:method :delete
                                             :headers {"authorization" (str "Bearer " fake-token)}
                                             :uri    (str "/api/social/note/" note-id)})))

      (flow "delete and check note response"
        (match? {:status 202
                 :body {:note-id note-id
                        :definition-id "clojure.core/disj"
                        :body "my edited note about this function."
                        :created-at string?}}
                (state-flow.server/request! {:method :delete
                                             :headers {"authorization" (str "Bearer " token)}
                                             :uri    (str "/api/social/note/" note-id)})))

      (flow "get-note api should return 404 for the deleted note"
        (match? {:status 404
                 :body "Not found."}
                (state-flow.server/request! {:method :get
                                             :uri (str "/api/social/note/" note-id)}))))))

(defflow
  flow-integration-see-also-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    (state-flow.http/set-http-out-responses! github-api-mocks)
    [config (state-flow.api/get-state :config)
     author-response (state-flow.server/request! {:method :post
                                                  :uri    "/api/login/github"
                                                  :body   {:code "agc622abb6135be5d1f2"}})
     :let [token (->> author-response :body :access-token)
           fake-token (create-token "fake-delboni" config)]]

    (flow "create & update see-also"
      [new-see-also-response (state-flow.server/request! {:method :post
                                                          :headers {"authorization" (str "Bearer " token)}
                                                          :uri    "/api/social/see-also/"
                                                          :body   {:definition-id "clojure.core/disj"
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
                                               :uri "/api/social/definition/clojure.core/disj"})))

        (flow "checks db for created see-also using get-see-also api"
          (match? {:status 200
                   :body {:see-also-id see-also-id
                          :definition-id "clojure.core/disj"
                          :definition-id-to "clojure.core/dissoc"
                          :created-at string?}}
                  (state-flow.server/request! {:method :get
                                               :uri (str "/api/social/see-also/" see-also-id)})))

        (flow "should not be able to delete if not allowed"
          (match? {:status 403
                   :body "You not allowed to delete this see also."}
                  (state-flow.server/request! {:method :delete
                                               :headers {"authorization" (str "Bearer " fake-token)}
                                               :uri    (str "/api/social/see-also/" see-also-id)})))

        (flow "delete and check see-also response"
          (match? {:status 202
                   :body {:see-also-id see-also-id
                          :definition-id "clojure.core/disj"
                          :definition-id-to "clojure.core/dissoc"
                          :created-at string?}}
                  (state-flow.server/request! {:method :delete
                                               :headers {"authorization" (str "Bearer " token)}
                                               :uri    (str "/api/social/see-also/" see-also-id)})))

        (flow "get-see-also api should return 404 for the deleted see-also"
          (match? {:status 404
                   :body "Not found."}
                  (state-flow.server/request! {:method :get
                                               :uri (str "/api/social/see-also/" see-also-id)})))))))

(defflow
  flow-integration-example-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    (state-flow.http/set-http-out-responses! github-api-mocks)
    [author-response (state-flow.server/request! {:method :post
                                                  :uri    "/api/login/github"
                                                  :body   {:code "agc622abb6135be5d1f2"}})
     :let [token (->> author-response :body :access-token)]]

    (flow "create & update example"
      [new-example-response (state-flow.server/request! {:method :post
                                                         :headers {"authorization" (str "Bearer " token)}
                                                         :uri    "/api/social/example/"
                                                         :body   {:definition-id "clojure.core/disj"
                                                                  :body "my example about this function."}})
       :let [example-id (-> new-example-response :body :example-id)]]

      (flow "check new example response"
        (match? {:status 201
                 :body {:example-id string?
                        :definition-id "clojure.core/disj"
                        :body "my example about this function."
                        :created-at string?}}
                new-example-response)

        (flow "checks db for new example"
          (match? {:status 200
                   :body {:definition-id "clojure.core/disj"
                          :examples [{:example-id example-id
                                      :definition-id "clojure.core/disj"
                                      :body "my example about this function."
                                      :created-at string?
                                      :editors [{:author-id string?
                                                 :login "delboni"
                                                 :account-source "github"
                                                 :avatar-url "https://my.pic/me.jpg"
                                                 :created-at string?
                                                 :edited-at string?}]}]}}
                  (state-flow.server/request! {:method :get
                                               :uri "/api/social/definition/clojure.core/disj"}))))

      (flow "check update example response"
        (match? {:status 201
                 :body {:example-id example-id
                        :definition-id "clojure.core/disj"
                        :body "my edited example about this function."
                        :created-at string?}}
                (state-flow.server/request! {:method :put
                                             :headers {"authorization" (str "Bearer " token)}
                                             :uri    "/api/social/example/"
                                             :body   {:example-id example-id
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
                                               :uri "/api/social/definition/clojure.core/disj"})))

        (flow "checks db for updated example using get-example api"
          (match? {:status 200
                   :body {:example-id example-id
                          :definition-id "clojure.core/disj"
                          :body "my edited example about this function."
                          :created-at string?
                          :editors [{:author-id string?
                                     :login "delboni"
                                     :account-source "github"
                                     :avatar-url "https://my.pic/me.jpg"
                                     :created-at string?
                                     :edited-at string?}
                                    {:author-id string?
                                     :login "delboni"
                                     :account-source "github"
                                     :avatar-url "https://my.pic/me.jpg"
                                     :created-at string?
                                     :edited-at string?}]}}
                  (state-flow.server/request! {:method :get
                                               :uri (str "/api/social/example/" example-id)})))

        (flow "delete example revision part 1"
          (state-flow.server/request! {:method :delete
                                       :headers {"authorization" (str "Bearer " token)}
                                       :uri (str "/api/social/example/" example-id)})
          (match? {:status 200
                   :body {:example-id example-id
                          :definition-id "clojure.core/disj"
                          :body "my example about this function."
                          :created-at string?
                          :editors [{:author-id string?
                                     :login "delboni"
                                     :account-source "github"
                                     :avatar-url "https://my.pic/me.jpg"
                                     :created-at string?
                                     :edited-at string?}]}}
                  (state-flow.server/request! {:method :get
                                               :uri (str "/api/social/example/" example-id)})))

        (flow "delete example revision part 2"
          (match? {:status 202
                   :body {:example-id example-id
                          :definition-id "clojure.core/disj"
                          :body "my example about this function."
                          :created-at string?}}
                  (state-flow.server/request! {:method :delete
                                               :headers {"authorization" (str "Bearer " token)}
                                               :uri (str "/api/social/example/" example-id)}))
          (match? {:status 404
                   :body "Not found."}
                  (state-flow.server/request! {:method :get
                                               :uri (str "/api/social/example/" example-id)})))))))

(defflow
  flow-integration-definition-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "should not return definition"
      (match? {:status 200
               :body {:definition-id "golang/go/math/abs/0"
                      :notes []
                      :examples []
                      :see-alsos []}}
              (state-flow.server/request! {:method :get
                                           :uri    "/api/social/definition/golang/go/math/abs/0"})))))
