(ns backend.logic.project
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as db]
            [ring.util.response :as resp]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [hugsql.core :refer [def-db-fns]]
            [backend.support.repo :as repo]
            [backend.support.ring :refer :all]
            [backend.support.entity :refer :all]
            [backend.support.validation :refer :all]
            ))

(def ^:private attributes
  {
   :code       {:required true :max-length 100 :pattern #"[a-z0-9_]*"}
   :name       {:required true :max-length 100}
   :visibility {:required true :enum [:public :private]}
   })

(defn- get-detail-uri
  [request data]
  (get-deploy-url request "projects/" (:code data)))

(def-db-fns "backend/logic/project.sql")

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
            response (-> (resp/created
                           (get-detail-uri request result)
                           (entity-result result))
                         (header-etag result))]
        (log/debug "Created project" result)
        response))))

(defn project-list
  [request]
  (let [ds (:ds request)
        params (:params request)]
    (log/debug "Listing projects" params)
    (db/with-db-transaction
      [tc ds]
      (let [data (list-all-projects tc)
            result (map (partial list-entity-result get-detail-uri request) data)
            response (resp/response result)]
        (log/debug "Listed projects" data)
        response))))

(defn project-read
  [request]
  (let [ds (:ds request)
        id (-> request :params :id)]
    (log/debug "Reading project" id)
    (db/with-db-transaction
      [tc ds]
      (let [result (read-project tc {:code id})
            response (-> (resp/response (entity-result result))
                         (header-etag result))]
        (log/debug "Read project" result)
        response))))
