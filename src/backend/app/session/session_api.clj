(ns backend.app.session.session-api
  (:require [ring.util.response :as resp]
            [backend.support.ring :refer :all]
            [backend.support.api :refer :all]
            [backend.support.util :refer :all]
            [backend.app.session.session-logic :refer :all]
            [backend.app.user.user-logic :refer :all]
            [backend.app.user.user-api :refer :all]
            ))

(defn session-api-create
  [{:keys [ds body]}]
  (let [result (update-user-entity-result :user (session-logic-create ds body))]
    {:status (status-code :created)
     :body   result}))

(defn session-api-list
  [{:keys [ds params]}]
  (let [data (session-logic-list-active ds params)
        result (map (partial update-user-entity-result :user) data)]
    (resp/response result)))

(defn session-api-delete
  [{:keys [ds session]}]
  (session-logic-expire ds session)
  response-no-content)
