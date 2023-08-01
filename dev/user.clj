(ns user
  (:require [codes.clj.docs.backend.server]
            [malli.dev :as dev]
            [malli.dev.pretty :as pretty]))

(defn tap [args] (println :tap args) args)

(defn start
  []
  (dev/start! {:report (pretty/thrower)}))

(defn stop
  []
  (dev/stop!))

(comment
  (start)
  (stop))
