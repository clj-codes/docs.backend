(ns integration.codes.clj.docs.backend.ops-test
  (:require [clojure.test :refer [use-fixtures]]
            [integration.codes.clj.docs.backend.util :as util]
            [parenthesin.helpers.malli :as helpers.malli]
            [parenthesin.helpers.state-flow.server.pedestal :as state-flow.server]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defflow
  flow-integration-ops-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"
    (flow "health check"
      (match? {:status 200 :body "OK"}
              (state-flow.server/request! {:method :get
                                           :uri    "/ops/health"})))))
