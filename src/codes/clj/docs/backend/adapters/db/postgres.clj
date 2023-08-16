(ns codes.clj.docs.backend.adapters.db.postgres
  (:require [codes.clj.docs.backend.schemas.db.postgres :as schemas]))

(defn db->author
  {:malli/schema [:=> [:cat schemas/UnionRow] schemas/Author]}
  [{:keys [author-id login account-source avatar-url created-at]}]
  {:author/author-id author-id
   :author/login login
   :author/account-source account-source
   :author/avatar-url avatar-url
   :author/created-at created-at})

(defn db->note
  {:malli/schema [:=> [:cat schemas/UnionRow] schemas/Note]}
  [{:keys [id definition-id body created] :as note}]
  {:note/note-id id
   :note/definition-id definition-id
   :note/body body
   :note/created-at created
   :note/author (db->author note)})

(defn db->notes
  {:malli/schema [:=> [:cat [:sequential schemas/UnionRow]] [:sequential schemas/Note]]}
  [db-rows]
  (->> db-rows
       (filter #(= (:type %) "note"))
       (map db->note)))

(defn db->example
  {:malli/schema [:=> [:cat schemas/UnionRow [:sequential schemas/Author]] schemas/Example]}
  [{:keys [id definition-id body created] :as example}
   editors]
  {:example/example-id id
   :example/definition-id definition-id
   :example/body body
   :example/created-at created
   :example/author (db->author example)
   :example/editors editors})

(defn db->examples
  {:malli/schema [:=> [:cat [:sequential schemas/UnionRow]] [:sequential schemas/Example]]}
  [db-rows]
  (->> db-rows
       (filter #(= (:type %) "example"))
       (group-by :id)
       (map (fn [[_ examples]]
              (let [sorted-examples (sort-by :created examples)
                    editors (map db->author sorted-examples)
                    example (last sorted-examples)]
                (db->example example editors))))))

(defn db->see-also
  {:malli/schema [:=> [:cat schemas/UnionRow] schemas/SeeAlso]}
  [{:keys [id definition-id body created] :as see-also}]
  {:see-also/see-also-id id
   :see-also/definition-id definition-id
   :see-also/definition-id-to body
   :see-also/created-at created
   :see-also/author (db->author see-also)})

(defn db->see-alsos
  {:malli/schema [:=> [:cat [:sequential schemas/UnionRow]] [:sequential schemas/SeeAlso]]}
  [db-rows]
  (->> db-rows
       (filter #(= (:type %) "see-also"))
       (map db->see-also)))

(defn db->definitions
  {:malli/schema [:=> [:cat [:sequential schemas/UnionRow]] [:sequential schemas/Definition]]}
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
