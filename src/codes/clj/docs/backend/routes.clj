(ns codes.clj.docs.backend.routes
  (:require [codes.clj.docs.backend.ports.http-in :as ports.http-in]
            [codes.clj.docs.backend.schemas.wire :as schemas.wire]
            [codes.clj.docs.backend.schemas.wire.in :as schemas.wire.in]
            [codes.clj.docs.backend.schemas.wire.out.document :as schemas.wire.out.document]
            [codes.clj.docs.backend.schemas.wire.out.social :as schemas.wire.out.social]
            [reitit.swagger :as swagger]))

(def routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "clj.docs"
                            :description "codes.clj.docs.backend"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/social"

    ["/author"
     {:swagger {:tags ["author" "social"]}}

     ["/"
      {:post {:summary "create new author"
              :parameters {:body schemas.wire.in/NewAuthor}
              :responses {201 {:body schemas.wire/Author}
                          400 {:body :string}
                          403 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in/upsert-author}}]

     ["/:login/:source"
      {:get {:summary "get author by login and source"
             :parameters {:path {:login :string
                                 :source :string}}
             :responses {200 {:body schemas.wire/Author}
                         400 {:body :string}
                         404 {:body :string}
                         500 {:body :string}}
             :handler ports.http-in/get-author}}]]

    ["/example"
     {:swagger {:tags ["example" "social"]}}

     ["/"
      {:post {:summary "create new example"
              :parameters {:body schemas.wire.in/NewExample}
              :responses {201 {:body schemas.wire.out.social/Example}
                          400 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in/insert-example}

       :put {:summary "update example by its id"
             :parameters {:body schemas.wire.in/UpdateExample}
             :responses {201 {:body schemas.wire.out.social/Example}
                         400 {:body :string}
                         403 {:body :string}
                         500 {:body :string}}
             :handler ports.http-in/update-example}}]]

    ["/note"
     {:swagger {:tags ["note" "social"]}}

     ["/"
      {:post {:summary "create new note"
              :parameters {:body schemas.wire.in/NewNote}
              :responses {201 {:body schemas.wire.out.social/Note}
                          400 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in/insert-note}

       :put {:summary "update note by its id"
             :parameters {:body schemas.wire.in/UpdateNote}
             :responses {201 {:body schemas.wire.out.social/Note}
                         400 {:body :string}
                         403 {:body :string}
                         500 {:body :string}}
             :handler ports.http-in/update-note}}]]

    ["/see-also"
     {:swagger {:tags ["see-also" "social"]}}

     ["/"
      {:post {:summary "create new see-also"
              :parameters {:body schemas.wire.in/NewSeeAlso}
              :responses {201 {:body schemas.wire.out.social/SeeAlso}
                          400 {:body :string}
                          500 {:body :string}}
              :handler ports.http-in/insert-see-also}}]]]

   ["/document"

    ["/projects"
     {:swagger {:tags ["projects" "document"]}}

     ["/"
      {:get {:summary "get project list"
             :responses {200 {:body schemas.wire.out.document/Projects}}
             :handler ports.http-in/get-projects}}]]]])
