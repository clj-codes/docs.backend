(ns codes.clj.docs.backend.ports.http-in.ops)

(defn health
  [_]
  {:status 200 :body "OK"})
