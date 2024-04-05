(ns codes.clj.docs.backend.config
  (:require [clojure.string :as string]))

(def vector-env-vars
  "Nested keys for config variables with corresponding environment 
  variables that can hold multiple values separated by commas, e.g.:
  ALLOWED_ORIGINS=\"https://domain.a.com, https://domain.b.com\""
  [[:config :webserver/allowed-origins]])

(defn- str-var->vector-var
  "Converts a string config variable to a vector of strings, when applicable.
  Environment variables are expected to be set as comma-separated values."
  [config nested-keys]
  (let [target-config (get-in config nested-keys)]
    (if (string? target-config)
      (let [split-configs (-> target-config
                              (string/split  #", "))
            env-config (map string/trim split-configs)]
        (assoc-in config nested-keys env-config))
      config)))

(defn- resolved-envs-config
  [config str-envs]
  (reduce str-var->vector-var config str-envs))

(defn config
  [config]
  (resolved-envs-config config vector-env-vars))
