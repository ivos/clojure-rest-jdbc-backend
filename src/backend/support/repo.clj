(ns backend.support.repo
  (:require [clojure.java.jdbc :as db]
            [clojure.tools.logging :as log]))

(def generated-key (keyword "scope_identity()"))

(defn- set-values [values]
  (let [version (:version values)
        no-id (dissoc values :id)]
    (assoc no-id :version (inc version))))

(defn- where-values [values]
  ["id = ? and version = ?" (:id values) (:version values)])

(defn- verify-result [result table values]
  (if-not (== 1 result)
    (throw (RuntimeException. (str "Conflict on " table " " values)))))

(defn create! [db table values]
  (log/debug "Inserting" table values)
  (let [defaulted-values (assoc values :version 1)
        [result] (db/insert! db table defaulted-values)]
    (assoc defaulted-values :id (generated-key result))))

(defn update! [db table values]
  (log/debug "Updating" table values)
  (let [values-to-set (set-values values)
        [result] (db/update! db table values-to-set (where-values values))]
    (verify-result result table values)
    (merge values values-to-set)))

(defn delete! [db table values]
  (log/debug "Deleting" table values)
  (let [[result] (db/delete! db table (where-values values))]
    (verify-result result table values)
    values))
