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
   :code        {:required true :max-length 100 :pattern #"[a-z0-9_]*"}
   :name        {:required true :max-length 100}
   :visibility  {:required true :enum [:public :private]}
   :description {:max-length 500}
   :start       {}
   :duration    {}
   :budget      {}
   :created     {}
   })

(defn- get-detail-uri
  [request entity]
  (get-deploy-url request "projects/" (:code entity)))

(def-db-fns "backend/logic/project.sql")

(defn project-create
  [{ds :ds entity :body :as request}]
  (log/debug "Creating project" entity)
  (validate attributes entity)
  (db/with-db-transaction
    [tc ds]
    (let [now (t/now)
          entity (assoc entity :created (tc/to-sql-time now))
          result (repo/create! tc :project entity)
          response (resp/created (get-detail-uri request result))]
      (log/debug "Created project" result)
      response)))

(defn project-list
  [{:keys [ds params] :as request}]
  (log/debug "Listing projects" params)
  (db/with-db-transaction
    [tc ds]
    (let [data (list-all-projects tc)
          result (map (partial list-entity-result get-detail-uri attributes request) data)
          response (resp/response result)]
      (log/debug "Listed projects" data)
      response)))

(defn project-read
  [{:keys [ds params]}]
  (log/debug "Reading project" params)
  (db/with-db-transaction
    [tc ds]
    (let [result (read-project tc params)
          response (-> (resp/response (entity-result attributes result))
                       (etag-header result))]
      (log/debug "Read project" result)
      response)))

(defn project-update
  [{:keys [ds body params] :as request}]
  (let [version (get-version request)
        entity (assoc body :version version)
        where (assoc params :version version)]
    (log/debug "Updating project" entity "where" where)
    (validate attributes body)
    (db/with-db-transaction
      [tc ds]
      (let [result (repo/update! tc :project entity where)
            response (-> response-no-content
                         (location-header (get-detail-uri request result)))]
        (log/debug "Updated project" result)
        response))))

(defn project-delete
  [{:keys [ds params] :as request}]
  (let [version (get-version request)
        where (assoc params :version version)]
    (log/debug "Deleting project where" where)
    (db/with-db-transaction
      [tc ds]
      (repo/delete! tc :project where)
      (log/debug "Deleted project where" where)
      response-no-content)))
