(ns backend.app.session.session-api
  (:require [ring.util.response :as resp]
            [backend.support.ring :refer [status-code response-no-content]]
            [backend.app.session.session-logic :as logic]
            [backend.app.user.user-api :as user]
            ))

(defn session-api-create
  [{:keys [ds body]}]
  (let [result (user/update-user-entity-result :user (logic/session-logic-create ds body))]
    {:status (status-code :created)
     :body   result}))

(defn session-api-list
  [{:keys [ds params]}]
  (let [data (logic/session-logic-list-active ds params)
        result (map (partial user/update-user-entity-result :user) data)]
    (resp/response result)))

(defn session-api-delete
  [{:keys [ds session]}]
  (logic/session-logic-expire ds session)
  response-no-content)

(defn session-api-switch-to
  [{:keys [ds params]}]
  (let [result (user/update-user-entity-result :user (logic/session-logic-switch-to-user ds params))]
    {:status (status-code :created)
     :body   result}))
