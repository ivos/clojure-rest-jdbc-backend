(ns backend.support.repo
  (:require [clojure.java.jdbc :as db]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [backend.support.ring :refer [status-code]]))

(def ^:private generated-key
  (keyword "scope_identity()"))

(defn- entity-to-set
  [entity]
  (let [version (:version entity)
        no-id (dissoc entity :id)]
    (assoc no-id :version (inc version))))

(defn- where-clause
  [where]
  (let [clause (string/join " and " (map #(str (name %1) " = ?") (keys where)))]
    (vec (conj (vals where) clause))))

(defn- verify-result
  [result table where]
  (when-not (== 1 result)
    (log/debug "Conflict on" table "where" where)
    (throw+
      {:type     :custom-response
       :response {:status (status-code :precondition-failed)}})))

(defn create!
  [db table entity]
  (let [entity (assoc entity :version 1)
        db-entity (transform-keys csk/->snake_case entity)]
    (log/debug "Inserting" table db-entity)
    (let [[result] (db/insert! db table db-entity)]
      (assoc entity :id (generated-key result)))))

(defn update!
  ([db table entity]
   (let [where (select-keys entity [:id :version])]
     (update! db table entity where)))
  ([db table entity where]
   (let [set-map (entity-to-set entity)
         db-set-map (transform-keys csk/->snake_case set-map)
         db-where (transform-keys csk/->snake_case where)]
     (log/debug "Updating" table db-set-map "where" db-where)
     (let [[result] (db/update! db table db-set-map (where-clause db-where))]
       (verify-result result table where)
       (merge entity set-map)))))

(defn delete!
  [db table where]
  (let [db-where (transform-keys csk/->snake_case where)]
    (log/debug "Deleting" table "where" db-where)
    (let [[result] (db/delete! db table (where-clause db-where))]
      (verify-result result table where))))
