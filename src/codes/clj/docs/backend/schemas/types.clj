(ns codes.clj.docs.backend.schemas.types
  (:require [codes.clj.docs.backend.components.db-docs :as components.db-docs]
            [com.stuartsierra.component :as component]
            [malli.core :as m]
            [malli.experimental.time.generator]
            [parenthesin.components.db.jdbc-hikari :as components.db]
            [parenthesin.components.http.clj-http :as components.http]))

(def HttpComponent
  (m/-simple-schema
   {:type :http-component
    :pred #(satisfies? components.http/HttpProvider %)
    :type-properties {:error/message "should satisfy parenthesin.components.http/HttpProvider protocol."}}))

(def DatabaseComponent
  (m/-simple-schema
   {:type :database-component
    :pred #(satisfies? components.db/DatabaseProvider %)
    :type-properties {:error/message "should satisfy parenthesin.components.database/DatabaseProvider protocol."}}))

(def DatalevinComponent
  (m/-simple-schema
   {:type :datalevin-component
    :pred #(satisfies? components.db-docs/DbDocsProvider %)
    :type-properties {:error/message "should satisfy codes.clj.docs.backend.components.db-docs/DbDocsProvider protocol."}}))

(def GenericComponent
  (m/-simple-schema
   {:type :generic-component
    :pred #(satisfies? component/Lifecycle %)
    :type-properties {:error/message "should satisfy com.stuartsierra.component/Lifecycle protocol."}}))

(def TimeInstant
  [inst? {:min #inst "2000-01-01"
          :max #inst "2040-01-01"}])

(def Components
  [:map
   [:config GenericComponent]
   [:http HttpComponent]
   [:router GenericComponent]
   [:database DatabaseComponent]
   [:db-docs DatalevinComponent]])
