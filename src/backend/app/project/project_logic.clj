(ns backend.app.project.project-logic
  (:refer-clojure :exclude [list read update])
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as db]
            [hugsql.core :as sql]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [flatland.ordered.map :refer [ordered-map]]
            [backend.support.repo :as repo]
            [backend.support.entity :as entity]
            [backend.support.validation :as validation]
            [backend.app.user.user-logic :as user]
            ))

(def attributes
  (ordered-map
    :code {:required   true
           :max-length 100
           :pattern    #"[a-z0-9_]*"}
    :name {:required   true
           :max-length 100}
    :visibility {:required true
                 :enum     [:public :private]}
    :description {:max-length 500}
    :start {:type :date}
    :duration {:type :integer}
    :budget {:type :number}
    :dailyMeetingAt {:type :time}
    :kickOff {:type :timestamp}
    :created {:direction :out
              :type      :timestamp}
    :owner {:direction :out}
    ))

(sql/def-db-fns "backend/app/project/project.sql")

(defn- validate-unique-code-on-create
  [tc entity]
  (when (sql-read tc (select-keys entity [:owner :code]))
    (validation/failure {:code [[:duplicate]]})))

(defn create
  [ds session body]
  (log/debug "Creating project" body)
  (let [now (t/now)
        entity (-> (validation/valid attributes body)
                   (assoc :created (tc/to-sql-time now)
                          :owner (get-in session [:user :id])))]
    (db/with-db-transaction
      [tc ds]
      (validate-unique-code-on-create tc entity)
      (let [result (repo/create! tc :project entity)]
        (log/debug "Created project" result)
        result))))

(defn list
  [ds params]
  (log/debug "Listing projects" params)
  (db/with-db-transaction
    [tc ds]
    (let [result (->> (sql-list-all tc)
                      (map entity/entity-listed)
                      (entity/expand-list tc user/sql-expand :owner :id))]
      (log/debug "Listed projects" result)
      result)))

(defn read
  [ds session params]
  (let [current-user-id (get-in session [:user :id])
        where (assoc params :owner current-user-id)]
    (log/debug "Reading project" where)
    (db/with-db-transaction
      [tc ds]
      (let [result (->> (sql-read tc where)
                        (entity/entity-read)
                        (entity/expand-entity tc user/sql-expand :owner))]
        (log/debug "Read project" result)
        result))))

(defn- validate-unique-code-on-update
  [tc entity where]
  (when (and (not= (:code entity) (:code where))
             (sql-read tc {:owner (:owner where)
                           :code  (:code entity)}))
    (validation/failure {:code [[:duplicate]]})))

(defn update
  [ds session body params version]
  (let [current-user-id (get-in session [:user :id])
        where (assoc params :owner current-user-id
                            :version version)]
    (log/debug "Updating project" body "where" where)
    (let [entity (validation/valid attributes body)]
      (db/with-db-transaction
        [tc ds]
        (validate-unique-code-on-update tc entity where)
        (let [result (repo/update! tc :project entity where)]
          (log/debug "Updated project" result)
          result)))))

(defn delete
  [ds session params version]
  (let [current-user-id (get-in session [:user :id])
        where (assoc params :owner current-user-id
                            :version version)]
    (log/debug "Deleting project where" where)
    (db/with-db-transaction
      [tc ds]
      (repo/delete! tc :project where)
      (log/debug "Deleted project where" where))))
