(ns backend.app.session.session-logic
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as db]
            [hugsql.core :as sql]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [flatland.ordered.map :refer [ordered-map]]
            [buddy.hashers :as hashers]
            [backend.support.repo :as repo]
            [backend.support.entity :as entity]
            [backend.support.validation :as validation]
            [backend.support.util :as util]
            [backend.app.user.user-logic :as user]
            )
  (:import (java.util UUID)))

(def session-attributes
  (ordered-map
    :username {:direction :in
               :required  true}
    :password {:direction :in
               :required  true}
    :token {:direction :out}
    :created {:direction :out}
    :duration {:direction :out}
    :expires {:direction :out}
    :user {:direction :out}
    ))

(sql/def-db-fns "backend/app/session/session.sql")

(defn- calculate-expires
  [duration now]
  (tc/to-sql-time (t/plus now (t/minutes duration))))

(defn- create-internal
  [ds entity switch-to]
  (db/with-db-transaction
    [tc ds]
    (let [user (user/read ds (select-keys entity [:username]))]
      (when-not switch-to
        (when (not= "active" (:status user))
          (validation/failure {:user [[:invalid]]}))
        (when (not (hashers/check (:password entity) (:passwordHash user)))
          (validation/failure {:password [[:invalid]]})))
      (let [now (t/now)
            duration 90
            entity {:token    (str (UUID/randomUUID))
                    :created  (tc/to-sql-time now)
                    :duration duration
                    :expires  (calculate-expires duration now)
                    :user     (:id user)}]
        (db/insert! tc :session entity)
        (assoc entity :user user)))))

(defn create
  [ds body]
  (log/debug "Creating session" (util/filter-password body))
  (let [entity (validation/valid session-attributes body)
        result (create-internal ds entity false)]
    (log/debug "Created session" result)
    result))

(defn switch-to-user
  [ds params]
  (log/debug "Creating switch-to session" (util/filter-password params))
  (let [result (create-internal ds params true)]
    (log/debug "Created switch-to session" result)
    result))

(defn list-active
  [ds params]
  (log/debug "Listing active sessions" params)
  (db/with-db-transaction
    [tc ds]
    (let [now (t/now)
          result (->> (sql-list-active tc {:now (tc/to-sql-time now)})
                      (map entity/entity-listed)
                      (entity/expand-list tc user/sql-expand :user :id))]
      (log/debug "Listed active sessions" result)
      result)))

(defn read-active
  [ds token]
  (log/debug "Reading active session for token" token)
  (db/with-db-transaction
    [tc ds]
    (let [now (t/now)
          found (sql-read-active tc {:token token
                                     :now   (tc/to-sql-time now)})]
      (when found
        (let [duration (:duration found)
              expires (calculate-expires duration now)
              found (assoc found :expires expires)
              entity {:expires expires}
              where (select-keys found [:token])]
          (log/debug "Touching session" entity "where" where)
          (db/update! tc :session entity (repo/where-clause where))
          (let [result (->> found
                            entity/entity-listed
                            (entity/expand-entity tc user/sql-expand :user))]
            (log/debug "Read and touched active session" result)
            result))))))

(defn expire
  [ds session]
  (let [now (t/now)
        entity {:expires (tc/to-sql-time now)}
        where (select-keys session [:token])]
    (log/debug "Expiring session" entity "where" where)
    (db/with-db-transaction
      [tc ds]
      (db/update! tc :session entity (repo/where-clause where))
      (log/debug "Expired session where" where))))
