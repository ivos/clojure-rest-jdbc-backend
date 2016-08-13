(ns backend.logic.project
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as db]
            [ring.util.response :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [backend.support.repo :as repo]
            [backend.support.ring :refer :all]
            [backend.support.validation :refer :all]
            ))

(def ^:private attributes
  {
   :name       {:required true :max-length 100}
   :code       {:required true :max-length 100}
   :visibility {:required true :enum [:public :private]}
   })

(defn- get-detail-uri
  [request data]
  (get-url request "projects/" (:code data)))

(defn project-create
  [request]
  (let [ds (:ds request)
        data (:body request)]
    (log/debug "Creating project" data)
    (validate attributes data)
    (db/with-db-transaction
      [tc ds]
      (let [now (t/now)
            data (assoc data :created (tc/to-sql-time now))
            result (repo/create! tc :project data)
            response (-> (created (get-detail-uri request result) (entity-result result))
                         (header-etag result))]
        (log/debug "Created project" result)
        response))))
