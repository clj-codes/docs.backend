(ns codes.clj.docs.backend.config
  (:require [aero.core :as aero]
            [clojure.string :as string]))

(defn- str-var->vector-var
  "Converts a string config variable to a vector of strings, when applicable.
  Environment variables are expected to be set as comma-separated values."
  [value]
  (if (string? value)
    (let [split-configs (-> value
                            (string/split  #","))
          env-config (->> split-configs
                          (map string/trim)
                          (remove empty?))]
      env-config)
    value))

(defmethod aero/reader 'csv
  [_ _ value]
  (str-var->vector-var value))
