(ns backend.app.user.user-logic
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as db]
            [hugsql.core :refer [def-db-fns]]
            [flatland.ordered.map :refer [ordered-map]]
            [backend.support.repo :as repo]
            [backend.support.entity :refer :all]
            [backend.support.validation :refer :all]
            [backend.support.util :refer :all]
            ))

(def email-pattern
  #"^[_A-Za-z0-9-\+]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$")

(def user-attributes
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
    ))

(def-db-fns "backend/app/user/user.sql")

(defn- validate-unique-username-on-create
  [tc entity]
  (when (read-user tc (select-keys entity [:username]))
    (validation-failure {:username [[:duplicate]]})))

(defn- validate-unique-email-on-create
  [tc entity]
  (when (read-user tc {:username (:email entity)})
    (validation-failure {:email [[:duplicate]]})))

(defn user-logic-create
  [ds body]
  (log/debug "Creating user" (filter-password body))
  (let [attributes (assoc-in user-attributes [:password :required] true)
        entity (-> (valid attributes body)
                   hash-entity
                   (assoc :status "active"))]
    (db/with-db-transaction
      [tc ds]
      (validate-unique-username-on-create tc entity)
      (validate-unique-email-on-create tc entity)
      (let [result (repo/create! tc :user entity)]
        (log/debug "Created user" (filter-password result))
        result))))

(defn user-logic-list
  [ds params]
  (log/debug "Listing users" params)
  (db/with-db-transaction
    [tc ds]
    (let [result (->> (list-all-users tc)
                      (map entity-listed)
                      (map filter-password))]
      (log/debug "Listed users" result)
      result)))

(defn user-logic-read
  [ds params]
  (log/debug "Reading user" params)
  (db/with-db-transaction
    [tc ds]
    (let [result (->> (read-user tc params)
                      entity-read)]
      (log/debug "Read user" result)
      result)))

(defn- validate-unique-username-on-update
  [tc entity where]
  (when (and (not= (:username entity) (:username where))
             (read-user tc (select-keys entity [:username])))
    (validation-failure {:username [[:duplicate]]})))

(defn- validate-unique-email-on-update
  [tc entity where]
  (when-let [found (read-user tc {:username (:email entity)})]
    (when (not= (:username found) (:username where))
      (validation-failure {:email [[:duplicate]]}))))

(defn user-logic-update
  [ds body params version]
  (let [where (assoc params :version version)]
    (log/debug "Updating user" (filter-password body) "where" where)
    (let [entity (-> (valid user-attributes body)
                     hash-entity)]
      (db/with-db-transaction
        [tc ds]
        (validate-unique-username-on-update tc entity where)
        (validate-unique-email-on-update tc entity where)
        (let [result (repo/update! tc :user entity where)]
          (log/debug "Updated user" (filter-password result))
          result)))))

(defn- validate-status
  [tc where expected]
  (when-let [found (read-user tc (select-keys where [:username]))]
    (let [actual (:status found)]
      (when (not (contains? expected actual))
        (validation-failure {:status [[:invalid actual expected]]})))))

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

(defn user-logic-disable
  [ds params version]
  (perform-action ds params version "disable" #{"active"} "disabled"))

(defn user-logic-activate
  [ds params version]
  (perform-action ds params version "activate" #{"disabled"} "active"))

(defn user-logic-delete
  [ds params version]
  (let [where (assoc params :version version)]
    (log/debug "Deleting user where" where)
    (db/with-db-transaction
      [tc ds]
      (repo/delete! tc :user where)
      (log/debug "Deleted user where" where))))
