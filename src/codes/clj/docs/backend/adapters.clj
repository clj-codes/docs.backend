(ns codes.clj.docs.backend.adapters
  (:require [codes.clj.docs.backend.schemas.db :as schemas.db]
            [codes.clj.docs.backend.schemas.types :as schemas.types])
  (:import [java.time ZoneId]
           [java.time.format DateTimeFormatter]))

(defn ^:private date->localdatetime
  {:malli/schema [:=> [:cat inst? schemas.types/JavaZoneId] schemas.types/JavaLocalDateTime]}
  [value zone-id]
  (-> value
      .toInstant
      (.atZone zone-id)
      .toLocalDateTime))

(defn inst->utc-formated-string
  {:malli/schema [:=> [:cat inst? :string] :string]}
  [inst str-format]
  (-> inst
      (date->localdatetime (ZoneId/of "UTC"))
      (.format (DateTimeFormatter/ofPattern str-format))))

; TODO: input schema & test
(defn db->author
  {:malli/schema [:=> [:cat :any] schemas.db/Author]}
  [{:keys [author-id login account-source avatar-url created-at]}]
  {:author/author-id author-id
   :author/login login
   :author/account-source account-source
   :author/avatar-url avatar-url
   :author/created-at created-at})

; TODO: input schema & test
(defn db->note
  {:malli/schema [:=> [:cat :any] schemas.db/Note]}
  [{:keys [id definition-id body created] :as note}]
  {:note/note-id id
   :note/definition-id definition-id
   :note/body body
   :note/created-at created
   :note/author (db->author note)})

; TODO: input schema & test
(defn db->notes
  {:malli/schema [:=> [:cat :any] [:sequential schemas.db/Note]]}
  [db-rows]
  (->> db-rows
       (filter #(= (:type %) "note"))
       (map db->note)))

; TODO: input schema & test
(defn db->example
  {:malli/schema [:=> [:cat :any [:sequential schemas.db/Author]] schemas.db/Example]}
  [{:keys [id definition-id body created] :as example}
   editors]
  {:example/example-id id
   :example/definition-id definition-id
   :example/body body
   :example/created-at created
   :example/author (db->author example)
   :example/editors editors})

; TODO: input schema & test
(defn db->examples
  {:malli/schema [:=> [:cat :any] [:sequential schemas.db/Example]]}
  [db-rows]
  (->> db-rows
       (filter #(= (:type %) "example"))
       (group-by :id)
       (map (fn [[_ examples]]
              (let [sorted-examples (sort-by :created examples)
                    editors (map db->author sorted-examples)
                    example (last sorted-examples)]
                (db->example example editors))))))

; TODO: input schema & test
(defn db->see-also
  {:malli/schema [:=> [:cat :any] schemas.db/SeeAlso]}
  [{:keys [id definition-id body created] :as see-also}]
  {:see-also/see-also-id id
   :see-also/definition-id definition-id
   :see-also/definition-id-to body
   :see-also/created-at created
   :see-also/author (db->author see-also)})

; TODO: input schema & test
(defn db->see-alsos
  {:malli/schema [:=> [:cat :any] [:sequential schemas.db/SeeAlso]]}
  [db-rows]
  (->> db-rows
       (filter #(= (:type %) "see-also"))
       (map db->see-also)))

; TODO: input schema
(defn db->definitions
  {:malli/schema [:=> [:cat :any] [:sequential schemas.db/Definition]]}
  [db-rows]
  (->> db-rows
       (group-by :definition-id)
       (map (fn [[definition-id items]]
              (let [notes (db->notes items)
                    examples (db->examples items)
                    see-alsos (db->see-alsos items)]
                {:definition/definition-id definition-id
                 :definition/notes notes
                 :definition/examples examples
                 :definition/see-alsos see-alsos})))))
