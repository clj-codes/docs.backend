(ns unit.codes.clj.docs.backend.adapters.social-test
  (:require [clojure.test :refer [use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [codes.clj.docs.backend.adapters.social :as adapters.social]
            [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [codes.clj.docs.backend.schemas.wire.in.social :as schemas.wire.in.social]
            [codes.clj.docs.backend.schemas.wire.out.social :as schemas.wire.out.social]
            [codes.clj.docs.backend.schemas.wire.social :as schemas.wire.social]
            [malli.core :as m]
            [malli.generator :as mg]
            [parenthesin.helpers.malli :as helpers.malli]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defspec upsert-author-wire->model-test 50
  (properties/for-all [author (mg/generator schemas.wire.in.social/NewAuthor)]
                      (m/validate schemas.model.social/NewAuthor (adapters.social/upsert-author-wire->model author))))

(defspec author->model->wire-test 50
  (properties/for-all [author (mg/generator schemas.model.social/Author)]
                      (m/validate schemas.wire.social/Author (adapters.social/author->model->wire author))))

(defspec new-example-wire->model-test 50
  (properties/for-all [example (mg/generator schemas.wire.in.social/NewExample)]
                      (m/validate schemas.model.social/NewExample (adapters.social/new-example-wire->model example))))

(defspec update-example-wire->model-test 50
  (properties/for-all [example (mg/generator schemas.wire.in.social/UpdateExample)]
                      (m/validate schemas.model.social/UpdateExample (adapters.social/update-example-wire->model example))))

(defspec example->model->wire-test 50
  (properties/for-all [example (mg/generator schemas.model.social/Example)]
                      (m/validate schemas.wire.out.social/Example (adapters.social/example->model->wire example))))

(defspec new-see-also-wire->model-test 50
  (properties/for-all [see-also (mg/generator schemas.wire.in.social/NewSeeAlso)]
                      (m/validate schemas.model.social/NewSeeAlso (adapters.social/new-see-also-wire->model see-also))))

(defspec see-also->model->wire-test 50
  (properties/for-all [see-also (mg/generator schemas.model.social/SeeAlso)]
                      (m/validate schemas.wire.out.social/SeeAlso (adapters.social/see-also->model->wire see-also))))

(defspec upsert-note-wire->model-test 50
  (properties/for-all [note (mg/generator schemas.wire.in.social/NewNote)]
                      (m/validate schemas.model.social/NewNote (adapters.social/new-note-wire->model note))))

(defspec update-note-wire->model-test 50
  (properties/for-all [note (mg/generator schemas.wire.in.social/UpdateNote)]
                      (m/validate schemas.model.social/UpdateNote (adapters.social/update-note-wire->model note))))

(defspec note->model->wire-test 50
  (properties/for-all [note (mg/generator schemas.model.social/Note)]
                      (m/validate schemas.wire.out.social/Note (adapters.social/note->model->wire note))))

(defspec social->model->wire-test 50
  (properties/for-all [definition (mg/generator schemas.model.social/Social)]
                      (m/validate schemas.wire.out.social/Social (adapters.social/social->model->wire definition))))
