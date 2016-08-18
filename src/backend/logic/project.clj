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
  [request values]
  (get-deploy-url request "projects/" (:code values)))

(def-db-fns "backend/logic/project.sql")

(defn project-create
  [request]
  (let [ds (:ds request)
        values (:body request)]
    (log/debug "Creating project" values)
    (validate attributes values)
    (db/with-db-transaction
      [tc ds]
      (let [now (t/now)
            values (assoc values :created (tc/to-sql-time now))
            result (repo/create! tc :project values)
            response (-> (resp/created
                           (get-detail-uri request result)
                           (entity-result result))
                         (etag-header result))]
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
        params (:params request)]
    (log/debug "Reading project" params)
    (db/with-db-transaction
      [tc ds]
      (let [result (read-project tc params)
            response (-> (resp/response (entity-result result))
                         (etag-header result))]
        (log/debug "Read project" result)
        response))))

(defn project-update
  [request]
  (let [ds (:ds request)
        version (get-version request)
        body (:body request)
        values (assoc body :version version)
        where (assoc (:params request) :version version)]
    (log/debug "Updating project" values "where" where)
    (validate attributes body)
    (db/with-db-transaction
      [tc ds]
      (let [result (repo/update! tc :project values where)
            response (-> response-no-content
                         (location-header (get-detail-uri request result))
                         (etag-header result))]
        (log/debug "Updated project" result)
        response))))
