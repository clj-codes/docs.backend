(ns codes.clj.docs.backend.adapters
  (:require [codes.clj.docs.backend.schemas.types :as schemas.types])
  (:import [java.time ZoneId]
           [java.time.format DateTimeFormatter]))

(defn ^:private date->localdatetime
  {:malli/schema [:=> [:cat inst? schemas.types/JavaZoneId] schemas.types/JavaLocalDateTime]}
  [value zone-id]
  (-> value
      .toInstant
      (.atZone zone-id)
      .toLocalDateTime))

(defn inst->utc-formated-string
  {:malli/schema [:=> [:cat inst? :string] :string]}
  [inst str-format]
  (-> inst
      (date->localdatetime (ZoneId/of "UTC"))
      (.format (DateTimeFormatter/ofPattern str-format))))
