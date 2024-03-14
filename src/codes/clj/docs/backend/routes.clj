(ns codes.clj.docs.backend.routes
  (:require [codes.clj.docs.backend.interceptors :as backend.interceptors]
            [codes.clj.docs.backend.ports.http-in.document :as ports.http-in.document]
            [codes.clj.docs.backend.ports.http-in.social :as ports.http-in.social]
            [codes.clj.docs.backend.schemas.wire.in.social :as schemas.wire.in.social]
            [codes.clj.docs.backend.schemas.wire.out.document :as schemas.wire.out.document]
            [codes.clj.docs.backend.schemas.wire.out.social :as schemas.wire.out.social]
            [codes.clj.docs.backend.schemas.wire.social :as schemas.wire.social]
            [reitit.swagger :as swagger]))

(def routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "clj.docs"
                            :description "codes.clj.docs.backend"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/api"

    ["/login"
     {:swagger {:tags ["login"]}}

     ["/github"
      {:post {:summary "author login with github"
              :parameters {:body {:code :string}}
              :responses {201 {:body {:author schemas.wire.social/Author
                                      :access-token :string}}
                          400 {:body :string}
                          403 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in.social/author-login-github}}]]

    ["/social"
     {:swagger {:tags ["social"]}}

     ["/author"
      ["/:login/:source"
       {:get {:summary "get author by login and source"
              :parameters {:path {:login :string
                                  :source :string}}
              :responses {200 {:body schemas.wire.social/Author}
                          400 {:body :string}
                          404 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in.social/get-author}}]]

     ["/example"
      ["/:example-id"
       {:get {:summary "get example by id"
              :parameters {:path {:example-id :uuid}}
              :responses {201 {:body schemas.wire.out.social/Example}
                          404 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in.social/get-example}}]
      ["/"
       {:interceptors [(backend.interceptors/auth-validate-jwt-interceptor)]
        :parameters {:header {:authorization :string}}

        :post {:summary "create new example"
               :parameters {:body schemas.wire.in.social/NewExample}
               :responses {201 {:body schemas.wire.out.social/Example}
                           400 {:body :string}
                           500 {:body :string}}
               :handler ports.http-in.social/insert-example}

        :put {:summary "update example by its id"
              :parameters {:body schemas.wire.in.social/UpdateExample}
              :responses {201 {:body schemas.wire.out.social/Example}
                          400 {:body :string}
                          403 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in.social/update-example}}]]

     ["/note"
      ["/:note-id"
       {:get {:summary "get note by id"
              :parameters {:path {:note-id :uuid}}
              :responses {201 {:body schemas.wire.out.social/Note}
                          404 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in.social/get-note}

        :delete {:summary "delete note by id"
                 :interceptors [(backend.interceptors/auth-validate-jwt-interceptor)]
                 :parameters {:header {:authorization :string}
                              :path {:note-id :uuid}}
                 :responses {202 {:body schemas.wire.out.social/Note}
                             403 {:body :string}
                             500 {:body :string}}
                 :handler ports.http-in.social/delete-note}}]
      ["/"
       {:interceptors [(backend.interceptors/auth-validate-jwt-interceptor)]
        :parameters {:header {:authorization :string}}

        :post {:summary "create new note"
               :parameters {:body schemas.wire.in.social/NewNote}
               :responses {201 {:body schemas.wire.out.social/Note}
                           400 {:body :string}
                           500 {:body :string}}
               :handler ports.http-in.social/insert-note}

        :put {:summary "update note by its id"
              :parameters {:body schemas.wire.in.social/UpdateNote}
              :responses {201 {:body schemas.wire.out.social/Note}
                          400 {:body :string}
                          403 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in.social/update-note}}]]

     ["/see-also"
      ["/:see-also-id"
       {:get {:summary "get see-also by id"
              :parameters {:path {:see-also-id :uuid}}
              :responses {201 {:body schemas.wire.out.social/SeeAlso}
                          404 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in.social/get-see-also}

        :delete {:summary "delete see-also by id"
                 :interceptors [(backend.interceptors/auth-validate-jwt-interceptor)]
                 :parameters {:header {:authorization :string}
                              :path {:see-also-id :uuid}}
                 :responses {202 {:body schemas.wire.out.social/SeeAlso}
                             403 {:body :string}
                             500 {:body :string}}
                 :handler ports.http-in.social/delete-see-also}}]
      ["/"
       {:interceptors [(backend.interceptors/auth-validate-jwt-interceptor)]
        :parameters {:header {:authorization :string}}

        :post {:summary "create new see-also"
               :parameters {:body schemas.wire.in.social/NewSeeAlso}
               :responses {201 {:body schemas.wire.out.social/SeeAlso}
                           400 {:body :string}
                           500 {:body :string}}
               :handler ports.http-in.social/insert-see-also}}]]

     ["/definition"
      ["/{*definition-id}"
       {:get {:summary "get definition socials list by id"
              :parameters {:path {:definition-id :string}}
              :responses {200 {:body schemas.wire.out.social/Social}
                          404 {:body :string}}
              :handler ports.http-in.social/get-by-definition}}]]]

    ["/document"
     {:swagger {:tags ["document"]}}

     ["/projects"
      ["/"
       {:get {:summary "get project list"
              :responses {200 {:body schemas.wire.out.document/Projects}}
              :handler ports.http-in.document/get-projects}}]]

     ["/namespaces"
      ["/{*project-id}"
       {:get {:summary "get namespace list by project id"
              :parameters {:path {:project-id :string}}
              :responses {200 {:body schemas.wire.out.document/ProjectNamespaces}
                          404 {:body :string}}
              :handler ports.http-in.document/get-namespaces-by-project}}]]

     ["/definitions"
      ["/{*namespace-id}"
       {:get {:summary "get definitions list by namespace id"
              :parameters {:path {:namespace-id :string}}
              :responses {200 {:body schemas.wire.out.document/ProjectNamespaceDefinitions}
                          404 {:body :string}}
              :handler ports.http-in.document/get-definitions-by-namespace}}]]

     ["/definition"
      ["/{*definition-id}"
       {:get {:summary "get definition by id"
              :parameters {:path {:definition-id :string}}
              :responses {200 {:body schemas.wire.out.document/ProjectNamespaceDefinition}
                          404 {:body :string}}
              :handler ports.http-in.document/get-definition-by-id}}]]

     ["/search"
      ["/"
       {:get {:summary "search documents by fulltext index"
              :parameters {:query [:map
                                   [:q :string]
                                   [:top {:optional true} :int]]}
              :responses {200 {:body schemas.wire.out.document/SearchResults}
                          404 {:body :string}}
              :handler ports.http-in.document/search-by-fulltext}}]]]]])
