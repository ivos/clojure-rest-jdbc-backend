(ns backend.app.user.user-logic
  (:refer-clojure :exclude [list read update])
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as db]
            [hugsql.core :as sql]
            [flatland.ordered.map :refer [ordered-map]]
            [backend.support.repo :as repo]
            [backend.support.entity :as entity]
            [backend.support.validation :as validation]
            [backend.support.util :as util]
            ))

(def email-pattern
  #"^[_A-Za-z0-9-\+]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$")

(def attributes
  (ordered-map
    :username {:required   true
               :max-length 100
               :pattern    #"[a-z0-9_]*"}
    :email {:required   true
            :max-length 100
            :pattern    email-pattern}
    :name {:required   true
           :max-length 100}
    :password {:max-length 100
               :direction  :in}
    :status {:direction :out}
    :roles {:direction :out}
    ))

(sql/def-db-fns "backend/app/user/user.sql")

(defn- validate-unique-username-on-create
  [tc entity]
  (when (sql-read tc (select-keys entity [:username]))
    (validation/validation-failure {:username [[:duplicate]]})))

(defn- validate-unique-email-on-create
  [tc entity]
  (when (sql-read tc {:username (:email entity)})
    (validation/validation-failure {:email [[:duplicate]]})))

(defn create
  [ds body]
  (log/debug "Creating user" (util/filter-password body))
  (let [attributes (assoc-in attributes [:password :required] true)
        entity (-> (validation/valid attributes body)
                   (util/hash-entity)
                   (assoc :status "active" :roles "user"))]
    (db/with-db-transaction
      [tc ds]
      (validate-unique-username-on-create tc entity)
      (validate-unique-email-on-create tc entity)
      (let [result (repo/create! tc :user entity)]
        (log/debug "Created user" (util/filter-password result))
        result))))

(defn list
  [ds params]
  (log/debug "Listing users" params)
  (db/with-db-transaction
    [tc ds]
    (let [result (->> (sql-list-all tc)
                      (map entity/entity-listed))]
      (log/debug "Listed users" result)
      result)))

(defn read
  [ds params]
  (log/debug "Reading user" params)
  (db/with-db-transaction
    [tc ds]
    (let [result (->> (sql-read tc params)
                      entity/entity-read)]
      (log/debug "Read user" result)
      result)))

(defn- validate-unique-username-on-update
  [tc entity where]
  (when (and (not= (:username entity) (:username where))
             (sql-read tc (select-keys entity [:username])))
    (validation/validation-failure {:username [[:duplicate]]})))

(defn- validate-unique-email-on-update
  [tc entity where]
  (when-let [found (sql-read tc {:username (:email entity)})]
    (when (not= (:username found) (:username where))
      (validation/validation-failure {:email [[:duplicate]]}))))

(defn update
  [ds body params version]
  (let [where (assoc params :version version)]
    (log/debug "Updating user" (util/filter-password body) "where" where)
    (let [entity (-> (validation/valid attributes body)
                     (util/hash-entity))]
      (db/with-db-transaction
        [tc ds]
        (validate-unique-username-on-update tc entity where)
        (validate-unique-email-on-update tc entity where)
        (let [result (repo/update! tc :user entity where)]
          (log/debug "Updated user" (util/filter-password result))
          result)))))

(defn- validate-status
  [tc where expected]
  (when-let [found (sql-read tc (select-keys where [:username]))]
    (let [actual (:status found)]
      (when (not (contains? expected actual))
        (validation/validation-failure {:status [[:invalid actual expected]]})))))

(defn- perform-action
  [ds params version action from to]
  (let [where (assoc params :version version)]
    (log/debug "Performing action" action "on user where" where)
    (db/with-db-transaction
      [tc ds]
      (validate-status tc where from)
      (let [entity {:status to}]
        (repo/update! tc :user entity where)
        (log/debug "Performed action" action "on user where" where)))))

(defn disable
  [ds params version]
  (perform-action ds params version "disable" #{"active"} "disabled"))

(defn activate
  [ds params version]
  (perform-action ds params version "activate" #{"disabled"} "active"))

(defn delete
  [ds params version]
  (let [where (assoc params :version version)]
    (log/debug "Deleting user where" where)
    (db/with-db-transaction
      [tc ds]
      (repo/delete! tc :user where)
      (log/debug "Deleted user where" where))))
