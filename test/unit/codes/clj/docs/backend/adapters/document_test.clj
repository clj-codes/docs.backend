(ns unit.codes.clj.docs.backend.adapters.document-test
  (:require [clojure.test :refer [use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [codes.clj.docs.backend.adapters.document :as adapters.document]
            [codes.clj.docs.backend.schemas.model.document :as schemas.model.document]
            [codes.clj.docs.backend.schemas.wire.out.document :as schemas.wire.out.document]
            [malli.core :as m]
            [malli.generator :as mg]
            [parenthesin.helpers.malli :as helpers.malli]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defspec project->wire 50
  (properties/for-all [projects (mg/generator schemas.model.document/Project)]
                      (m/validate schemas.wire.out.document/Project
                                  (adapters.document/project->wire projects))))

(defspec projects->wire 50
  (properties/for-all [projects (mg/generator schemas.model.document/Projects)]
                      (m/validate schemas.wire.out.document/Projects
                                  (adapters.document/projects->wire projects))))
