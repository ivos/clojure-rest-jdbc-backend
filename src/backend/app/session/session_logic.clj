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
                      :expires  (tc/to-sql-time (t/plus now (t/minutes duration)))
                      :user     (:id user)
                      }
              _ (db/insert! tc :session entity)
              result (assoc entity :user user)
              ]
          (log/debug "Created session" result)
          result)))))
