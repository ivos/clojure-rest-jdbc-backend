(ns backend.support.repo
  (:require [clojure.java.jdbc :as db]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
            [backend.support.ring :refer [status-code]]))

(def generated-key
  (keyword "scope_identity()"))

(defn- values-to-set
  [values]
  (let [version (:version values)
        no-id (dissoc values :id)]
    (assoc no-id :version (inc version))))

(defn- where-values
  [values]
  (let [clause (string/join " and " (map #(str (name %1) " = ?") (keys values)))]
    (vec (conj (vals values) clause))))

(defn- verify-result
  [result table where]
  (when-not (== 1 result)
    (log/debug "Conflict on" table "where" where)
    (throw+
      {:type     :custom-response
       :response {:status (status-code :precondition-failed)}})))

(defn create!
  [db table values]
  (log/debug "Inserting" table values)
  (let [defaulted-values (assoc values :version 1)
        [result] (db/insert! db table defaulted-values)]
    (assoc defaulted-values :id (generated-key result))))

(defn update!
  ([db table values]
   (let [where (select-keys values [:id :version])]
     (update! db table values where)))
  ([db table values where]
   (let [set-map (values-to-set values)]
     (log/debug "Updating" table set-map "where" where)
     (let [[result] (db/update! db table set-map (where-values where))]
       (verify-result result table where)
       (merge values set-map)))))

(defn delete!
  [db table values]
  (log/debug "Deleting" table values)
  (let [[result] (db/delete! db table (where-values values))]
    (verify-result result table values)
    values))
