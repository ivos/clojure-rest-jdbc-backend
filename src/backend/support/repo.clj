(ns backend.support.repo
  (:require [clojure.java.jdbc :as db]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
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
  (log/debug "Inserting" table entity)
  (let [defaulted-entity (assoc entity :version 1)
        [result] (db/insert! db table defaulted-entity)]
    (assoc defaulted-entity :id (generated-key result))))

(defn update!
  ([db table entity]
   (let [where (select-keys entity [:id :version])]
     (update! db table entity where)))
  ([db table entity where]
   (let [set-map (entity-to-set entity)]
     (log/debug "Updating" table set-map "where" where)
     (let [[result] (db/update! db table set-map (where-clause where))]
       (verify-result result table where)
       (merge entity set-map)))))

(defn delete!
  [db table where]
  (log/debug "Deleting" table "where" where)
  (let [[result] (db/delete! db table (where-clause where))]
    (verify-result result table where)))
