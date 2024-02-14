(ns codes.clj.docs.backend.routes
  (:require [codes.clj.docs.backend.ports.http-in.document :as ports.http-in.document]
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

    ["/social"

     ["/author"
      {:swagger {:tags ["author" "social"]}}

      ["/"
       {:post {:summary "create new author"
               :parameters {:body schemas.wire.in.social/NewAuthor}
               :responses {201 {:body schemas.wire.social/Author}
                           400 {:body :string}
                           403 {:body :string}
                           500 {:body :string}}
               :handler ports.http-in.social/upsert-author}}]

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
      {:swagger {:tags ["example" "social"]}}

      ["/"
       {:post {:summary "create new example"
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
      {:swagger {:tags ["note" "social"]}}

      ["/"
       {:post {:summary "create new note"
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
      {:swagger {:tags ["see-also" "social"]}}

      ["/"
       {:post {:summary "create new see-also"
               :parameters {:body schemas.wire.in.social/NewSeeAlso}
               :responses {201 {:body schemas.wire.out.social/SeeAlso}
                           400 {:body :string}
                           500 {:body :string}}
               :handler ports.http-in.social/insert-see-also}}]]

     ["/definition"
      {:swagger {:tags ["definition" "social"]}}

      ["/{*definition-id}"
       {:get {:summary "get definition socials list by id"
              :parameters {:path {:definition-id :string}}
              :responses {200 {:body schemas.wire.out.social/Social}
                          404 {:body :string}}
              :handler ports.http-in.social/get-by-definition}}]]]

    ["/document"

     ["/projects"
      {:swagger {:tags ["projects" "document"]}}

      ["/"
       {:get {:summary "get project list"
              :responses {200 {:body schemas.wire.out.document/Projects}}
              :handler ports.http-in.document/get-projects}}]]

     ["/namespaces"
      {:swagger {:tags ["namespaces" "document"]}}

      ["/{*project-id}"
       {:get {:summary "get namespace list by project id"
              :parameters {:path {:project-id :string}}
              :responses {200 {:body schemas.wire.out.document/ProjectNamespaces}
                          404 {:body :string}}
              :handler ports.http-in.document/get-namespaces-by-project}}]]

     ["/definitions"
      {:swagger {:tags ["definitions" "document"]}}

      ["/{*namespace-id}"
       {:get {:summary "get definitions list by namespace id"
              :parameters {:path {:namespace-id :string}}
              :responses {200 {:body schemas.wire.out.document/ProjectNamespaceDefinitions}
                          404 {:body :string}}
              :handler ports.http-in.document/get-definitions-by-namespace}}]]

     ["/definition"
      {:swagger {:tags ["definition" "document"]}}

      ["/{*definition-id}"
       {:get {:summary "get definition by id"
              :parameters {:path {:definition-id :string}}
              :responses {200 {:body schemas.wire.out.document/ProjectNamespaceDefinition}
                          404 {:body :string}}
              :handler ports.http-in.document/get-definition-by-id}}]]]]])
