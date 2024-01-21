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
  (-> namespace-schema
      (mu/select-keys [:id
                       :name
                       :group
                       :artifact
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
                       :row])
      (mu/assoc :project-id :string)))

(def Namespaces [:sequential Namespace])

(def Definition
  (-> definition
      (mu/select-keys [:id
                       :group
                       :artifact
                       :name
                       :private
                       :added
                       :arglist-strs
                       :col
                       :defined-by
                       :doc
                       :end-col
                       :end-row
                       :filename
                       :fixed-arities
                       :git-source
                       :macro
                       :meta
                       :name-col
                       :name-end-col
                       :name-end-row
                       :name-row
                       :protocol-name
                       :protocol-ns
                       :row
                       :varargs-min-arity])
      (mu/assoc :namespace-id :string)))

(def Definitions [:sequential Definition])
