(ns codes.clj.docs.backend.routes
  (:require [codes.clj.docs.backend.ports.http-in :as ports.http-in]
            [codes.clj.docs.backend.schemas.wire :as schemas.wire]
            [codes.clj.docs.backend.schemas.wire.in :as schemas.wire.in]
            [reitit.swagger :as swagger]))

(def routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "clj.docs"
                            :description "codes.clj.docs.backend"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/author"
    {:swagger {:tags ["author" "social"]}}

    ["/"
     {:post {:summary "create new author"
             :parameters {:body schemas.wire.in/NewAuthor}
             :responses {201 {:body {:id :uuid}}
                         403 {:body :string}
                         500 {:body :string}}
             :handler ports.http-in/upsert-author}}]

    ["/:login/:source"
     {:get {:summary "get author by login and source"
            :parameters {:path {:login :string
                                :source :string}}
            :responses {200 {:body schemas.wire/Author}
                        404 {:body :string}
                        500 {:body :string}}
            :handler ports.http-in/get-author}}]]])
