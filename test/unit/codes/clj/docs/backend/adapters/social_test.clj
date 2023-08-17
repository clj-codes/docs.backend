(ns unit.codes.clj.docs.backend.adapters.social-test
  (:require [clojure.test :refer [use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [codes.clj.docs.backend.adapters.social :as adapters.social]
            [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.wire :as schemas.wire]
            [codes.clj.docs.backend.schemas.wire.in :as schemas.wire.in]
            [codes.clj.docs.backend.schemas.wire.out :as schemas.wire.out]
            [malli.core :as m]
            [malli.generator :as mg]
            [parenthesin.helpers.malli :as helpers.malli]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defspec upsert-author-wire->model-test 50
  (properties/for-all [author (mg/generator schemas.wire.in/NewAuthor)]
                      (m/validate schemas.model.social/NewAuthor (adapters.social/upsert-author-wire->model author))))

(defspec author->model->wire-test 50
  (properties/for-all [author (mg/generator schemas.model.social/Author)]
                      (m/validate schemas.wire/Author (adapters.social/author->model->wire author))))

(defspec new-example-wire->model-test 50
  (properties/for-all [example (mg/generator schemas.wire.in/NewExample)]
                      (m/validate schemas.model.social/NewExample (adapters.social/new-example-wire->model example))))

(defspec update-example-wire->model-test 50
  (properties/for-all [example (mg/generator schemas.wire.in/UpdateExample)]
                      (m/validate schemas.model.social/UpdateExample (adapters.social/update-example-wire->model example))))

(defspec example->model->wire-test 50
  (properties/for-all [example (mg/generator schemas.model.social/Example)]
                      (m/validate schemas.wire.out/Example (adapters.social/example->model->wire example))))

(defspec new-see-also-wire->model-test 50
  (properties/for-all [see-also (mg/generator schemas.wire.in/NewSeeAlso)]
                      (m/validate schemas.model.social/NewSeeAlso (adapters.social/new-see-also-wire->model see-also))))

(defspec see-also->model->wire-test 50
  (properties/for-all [see-also (mg/generator schemas.model.social/SeeAlso)]
                      (m/validate schemas.wire.out/SeeAlso (adapters.social/see-also->model->wire see-also))))

(defspec upsert-note-wire->model-test 50
  (properties/for-all [note (mg/generator schemas.wire.in/NewNote)]
                      (m/validate schemas.model.social/NewNote (adapters.social/new-note-wire->model note))))

(defspec update-note-wire->model-test 50
  (properties/for-all [note (mg/generator schemas.wire.in/UpdateNote)]
                      (m/validate schemas.model.social/UpdateNote (adapters.social/update-note-wire->model note))))

(defspec note->model->wire-test 50
  (properties/for-all [note (mg/generator schemas.model.social/Note)]
                      (m/validate schemas.wire.out/Note (adapters.social/note->model->wire note))))

(defspec definition->model->wire-test 50
  (properties/for-all [definition (mg/generator schemas.model.social/Definition)]
                      (m/validate schemas.wire.out/Definition (adapters.social/definition->model->wire definition))))
