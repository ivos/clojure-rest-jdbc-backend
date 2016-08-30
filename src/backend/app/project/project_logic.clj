(ns backend.app.project.project-logic
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as db]
            [hugsql.core :refer [def-db-fns]]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [flatland.ordered.map :refer [ordered-map]]
            [backend.support.repo :as repo]
            [backend.support.entity :refer :all]
            [backend.support.validation :refer :all]
            [backend.app.user.user-logic :refer :all]
            ))

(def project-attributes
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

(def-db-fns "backend/app/project/project.sql")

(defn project-logic-create
  [ds session body]
  (log/debug "Creating project" body)
  (let [now (t/now)
        entity (-> (valid project-attributes body)
                   (assoc :created (tc/to-sql-time now)
                          :owner (get-in session [:user :id])))]
    (db/with-db-transaction
      [tc ds]
      (let [result (repo/create! tc :project entity)]
        (log/debug "Created project" result)
        result))))

(defn project-logic-list
  [ds params]
  (log/debug "Listing projects" params)
  (db/with-db-transaction
    [tc ds]
    (let [result (->> (list-all-projects tc)
                      (map entity-listed)
                      (expand-list tc expand-users :owner :id))]
      (log/debug "Listed projects" result)
      result)))

(defn project-logic-read
  [ds params]
  (log/debug "Reading project" params)
  (db/with-db-transaction
    [tc ds]
    (let [result (->> (read-project tc params)
                      entity-read
                      (expand-entity tc expand-users :owner))]
      (log/debug "Read project" result)
      result)))

(defn project-logic-update
  [ds body params version]
  (let [where (assoc params :version version)]
    (log/debug "Updating project" body "where" where)
    (let [entity (valid project-attributes body)]
      (db/with-db-transaction
        [tc ds]
        (let [result (repo/update! tc :project entity where)]
          (log/debug "Updated project" result)
          result)))))

(defn project-logic-delete
  [ds params version]
  (let [where (assoc params :version version)]
    (log/debug "Deleting project where" where)
    (db/with-db-transaction
      [tc ds]
      (repo/delete! tc :project where)
      (log/debug "Deleted project where" where))))
