(ns backend.app.session.session-api
  (:require
    ;[ring.util.response :as resp]
    [backend.support.ring :refer :all]
    [backend.support.api :refer :all]
    [backend.support.util :refer :all]
    [backend.app.session.session-logic :refer :all]
    [backend.app.user.user-logic :refer :all]
    ))

(defn session-api-create
  [{:keys [ds body]}]
  (let [result (session-logic-create ds body)
        result (update result :user
                       (comp (partial entity-result user-attributes)
                             filter-password))]
    {:status (status-code :created)
     :body   result}))
