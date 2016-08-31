(ns backend.app.session.session-api
  (:refer-clojure :exclude [list])
  (:require [ring.util.response :as resp]
            [backend.support.ring :refer [status-code response-no-content]]
            [backend.app.session.session-logic :as logic]
            [backend.app.user.user-api :as user]
            ))

(defn create
  [{:keys [ds body]}]
  (let [result (->> (logic/create ds body)
                    (user/update-entity-result :user))]
    {:status (status-code :created)
     :body   result}))

(defn list
  [{:keys [ds params]}]
  (let [data (logic/list-active ds params)
        result (map (partial user/update-entity-result :user) data)]
    (resp/response result)))

(defn delete
  [{:keys [ds session]}]
  (logic/expire ds session)
  response-no-content)

(defn switch-to
  [{:keys [ds params]}]
  (let [result (->> (logic/switch-to-user ds params)
                    (user/update-entity-result :user))]
    {:status (status-code :created)
     :body   result}))
