(ns codes.clj.docs.backend.adapters.db.postgres
  (:require [codes.clj.docs.backend.schemas.db.postgres :as schemas.db]
            [codes.clj.docs.backend.schemas.model.social :as schemas.model.social]
            [taoensso.encore :as enc]))

(defn db->author
  {:malli/schema [:=> [:cat schemas.db/AuthorRow] schemas.model.social/Author]}
  [{:keys [author-id login account-source avatar-url created-at]}]
  {:author/author-id author-id
   :author/login login
   :author/account-source account-source
   :author/avatar-url avatar-url
   :author/created-at created-at})

(defn db->note
  {:malli/schema [:=> [:cat schemas.db/Row] schemas.model.social/Note]}
  [{:keys [id definition-id body created author-id] :as note}]
  (enc/assoc-some {:note/note-id id
                   :note/definition-id definition-id
                   :note/body body
                   :note/created-at created}
                  :note/author (when author-id (db->author note))))

(defn db->notes
  {:malli/schema [:=> [:cat [:sequential schemas.db/Row]]
                  [:sequential schemas.model.social/Note]]}
  [db-rows]
  (->> db-rows
       (filter #(= (:type %) "note"))
       (map db->note)))

(defn db->example
  {:malli/schema [:=> [:cat schemas.db/Row [:sequential schemas.model.social/Author]]
                  schemas.model.social/Example]}
  [{:keys [id definition-id body created author-id] :as example}
   editors]
  (enc/assoc-some {:example/example-id id
                   :example/definition-id definition-id
                   :example/body body
                   :example/created-at created}
                  :example/author (when author-id (db->author example))
                  :example/editors (when (seq editors) editors)))

(defn db->examples
  {:malli/schema [:=> [:cat [:sequential schemas.db/Row]]
                  [:sequential schemas.model.social/Example]]}
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
  {:malli/schema [:=> [:cat schemas.db/Row] schemas.model.social/SeeAlso]}
  [{:keys [id definition-id body created author-id] :as see-also}]
  (enc/assoc-some {:see-also/see-also-id id
                   :see-also/definition-id definition-id
                   :see-also/definition-id-to body
                   :see-also/created-at created}
                  :see-also/author (when author-id
                                     (db->author see-also))))

(defn db->see-alsos
  {:malli/schema [:=> [:cat [:sequential schemas.db/Row]]
                  [:sequential schemas.model.social/SeeAlso]]}
  [db-rows]
  (->> db-rows
       (filter #(= (:type %) "see-also"))
       (map db->see-also)))

(defn db->social
  {:malli/schema [:=> [:cat [:sequential schemas.db/Row]]
                  [:maybe schemas.model.social/Social]]}
  [db-rows]
  (->> db-rows
       (group-by :definition-id)
       (map (fn [[definition-id items]]
              (let [notes (db->notes items)
                    examples (db->examples items)
                    see-alsos (db->see-alsos items)]
                {:social/definition-id definition-id
                 :social/notes notes
                 :social/examples examples
                 :social/see-alsos see-alsos})))
       first))
