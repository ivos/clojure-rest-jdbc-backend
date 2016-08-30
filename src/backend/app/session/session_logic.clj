(ns backend.app.session.session-logic
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as db]
            [ring.util.response :as resp]
            [hugsql.core :refer [def-db-fns]]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [flatland.ordered.map :refer [ordered-map]]
            [backend.support.repo :as repo]
            [backend.support.ring :refer :all]
            [backend.support.entity :refer :all]
            [backend.support.validation :refer :all]
            [backend.support.util :refer :all]
            [backend.app.user.user-logic :refer :all]
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

(def-db-fns "backend/app/session/session.sql")

(defn- calculate-expires
  [duration now]
  (tc/to-sql-time (t/plus now (t/minutes duration))))

(defn session-logic-create
  [ds body]
  (log/debug "Creating session" (filter-password body))
  (let [entity (-> (valid session-attributes body)
                   hash-entity)]
    (db/with-db-transaction
      [tc ds]
      (let [user (user-logic-read ds entity)]
        (when (not= "active" (:status user))
          (validation-failure {:user [[:invalid]]}))
        (when (not= (:passwordHash entity) (:passwordHash user))
          (validation-failure {:password [[:invalid]]}))
        (let [now (t/now)
              duration 90
              entity {
                      :token    (str (UUID/randomUUID))
                      :created  (tc/to-sql-time now)
                      :duration duration
                      :expires  (calculate-expires duration now)
                      :user     (:id user)
                      }
              _ (db/insert! tc :session entity)
              result (assoc entity :user user)
              ]
          (log/debug "Created session" result)
          result)))))

(defn session-logic-list-active
  [ds params]
  (log/debug "Listing active sessions" params)
  (db/with-db-transaction
    [tc ds]
    (let [now (t/now)
          result (->> (list-active-sessions tc {:now (tc/to-sql-time now)})
                      (map entity-listed)
                      (expand-list tc expand-users :user :id))]
      (log/debug "Listed active sessions" result)
      result)))

(defn session-logic-read-active
  [ds token]
  (log/debug "Reading active session for token" token)
  (db/with-db-transaction
    [tc ds]
    (let [now (t/now)
          found (read-active-session tc {:token token
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
                            entity-listed
                            (expand-entity tc expand-users :user))]
            (log/debug "Read and touched active session" result)
            result))))))

(defn session-logic-expire
  [ds session]
  (let [now (t/now)
        entity {:expires (tc/to-sql-time now)}
        where (select-keys session [:token])]
    (log/debug "Expiring session" entity "where" where)
    (db/with-db-transaction
      [tc ds]
      (db/update! tc :session entity (repo/where-clause where))
      (log/debug "Expired session where" where))))
