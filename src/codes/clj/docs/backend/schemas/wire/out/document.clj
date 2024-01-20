(ns codes.clj.docs.backend.schemas.wire.out.document
  (:require [codes.clj.docs.backend.schemas.wire :refer [definition
                                                         namespace-schema
                                                         project]]
            [malli.util :as mu]))

(def Project
  (mu/select-keys project [:id
                           :name
                           :group
                           :artifact
                           :paths
                           :url
                           :sha
                           :tag
                           :manifest]))

(def Projects [:sequential Project])

(def Namespace
  (mu/select-keys namespace-schema [:id
                                    :project
                                    :group
                                    :artifact
                                    :name
                                    :end-row
                                    :meta
                                    :name-end-col
                                    :name-end-row
                                    :name-row
                                    :added
                                    :author
                                    :filename
                                    :git-source
                                    :col
                                    :name-col
                                    :end-col
                                    :doc
                                    :row]))

(def Definition
  (mu/select-keys definition [:group
                              :artifact
                              :name
                              :defined-by
                              :namespace
                              :fixed-arities
                              :arglist-strs
                              :end-row
                              :meta
                              :name-end-col
                              :name-end-row
                              :added
                              :author
                              :filename
                              :git-source
                              :col
                              :name-col
                              :end-col
                              :macro
                              :varargs-min-arity
                              :private
                              :protocol-ns
                              :protocol-name]))
