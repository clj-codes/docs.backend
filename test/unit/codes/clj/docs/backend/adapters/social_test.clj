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

(defspec github-user-wire->model-test 50
  (properties/for-all [author (mg/generator schemas.wire.in.social/NewAuthorGithub)]
                      (m/validate schemas.model.social/NewAuthor
                                  (adapters.social/github-user-wire->model author))))

(defspec jwt-author->wire-test 50
  (properties/for-all [author (mg/generator schemas.wire.in.social/JwtAuthor)]
                      (m/validate schemas.wire.social/Author
                                  (adapters.social/jwt-author->wire author))))

(defspec author->model->wire-test 50
  (properties/for-all [author (mg/generator schemas.model.social/Author)]
                      (m/validate schemas.wire.social/Author
                                  (adapters.social/author->model->wire author))))

(defspec author-interaction->model->wire-test 50
  (properties/for-all [author+interaction (mg/generator schemas.model.social/Author+Interactions)]
                      (m/validate [:sequential schemas.wire.out.social/Author+Interactions]
                                  (adapters.social/author-interaction->model->wire [author+interaction]))))

(defspec editor->model->wire-test 50
  (properties/for-all [editor (mg/generator schemas.model.social/Editor)]
                      (m/validate schemas.wire.social/Editor
                                  (adapters.social/editor->model->wire editor))))

(defspec new-example-wire->model-test 50
  (properties/for-all [author-id (mg/generator :uuid)
                       example (mg/generator schemas.wire.in.social/NewExample)]
                      (m/validate schemas.model.social/NewExample
                                  (adapters.social/new-example-wire->model example author-id))))

(defspec update-example-wire->model-test 50
  (properties/for-all [author-id (mg/generator :uuid)
                       example (mg/generator schemas.wire.in.social/UpdateExample)]
                      (m/validate schemas.model.social/UpdateExample
                                  (adapters.social/update-example-wire->model example author-id))))

(defspec example->model->wire-test 50
  (properties/for-all [example (mg/generator schemas.model.social/Example)]
                      (m/validate schemas.wire.out.social/Example
                                  (adapters.social/example->model->wire example))))

(defspec new-see-also-wire->model-test 50
  (properties/for-all [author-id (mg/generator :uuid)
                       see-also (mg/generator schemas.wire.in.social/NewSeeAlso)]
                      (m/validate schemas.model.social/NewSeeAlso
                                  (adapters.social/new-see-also-wire->model see-also author-id))))

(defspec see-also->model->wire-test 50
  (properties/for-all [see-also (mg/generator schemas.model.social/SeeAlso)]
                      (m/validate schemas.wire.out.social/SeeAlso
                                  (adapters.social/see-also->model->wire see-also))))

(defspec upsert-note-wire->model-test 50
  (properties/for-all [author-id (mg/generator :uuid)
                       note (mg/generator schemas.wire.in.social/NewNote)]
                      (m/validate schemas.model.social/NewNote
                                  (adapters.social/new-note-wire->model note author-id))))

(defspec update-note-wire->model-test 50
  (properties/for-all [author-id (mg/generator :uuid)
                       note (mg/generator schemas.wire.in.social/UpdateNote)]
                      (m/validate schemas.model.social/UpdateNote
                                  (adapters.social/update-note-wire->model note author-id))))

(defspec note->model->wire-test 50
  (properties/for-all [note (mg/generator schemas.model.social/Note)]
                      (m/validate schemas.wire.out.social/Note
                                  (adapters.social/note->model->wire note))))

(defspec social->model->wire-test 50
  (properties/for-all [definition (mg/generator schemas.model.social/Social)]
                      (m/validate schemas.wire.out.social/Social
                                  (adapters.social/social->model->wire definition))))

(defspec author+socials->model->wire-test 50
  (properties/for-all [author+socials (mg/generator schemas.model.social/Author+Socials)]
                      (m/validate schemas.wire.out.social/Author+Socials
                                  (adapters.social/author+socials->model->wire author+socials))))

(defspec any-social->model->wire-test 50
  (properties/for-all [any-social (mg/generator schemas.model.social/AnySocial)]
                      (m/validate [:sequential schemas.wire.out.social/AnySocial]
                                  (adapters.social/any-social->model->wire [any-social]))))
