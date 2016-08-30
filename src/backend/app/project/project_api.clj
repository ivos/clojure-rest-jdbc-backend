(ns backend.app.project.project-api
  (:require [ring.util.response :as resp]
            [backend.support.ring :refer :all]
            [backend.support.api :refer :all]
            [backend.app.project.project-logic :refer :all]
            [backend.app.user.user-api :refer :all]
            ))

(defn- get-detail-uri
  [config entity]
  (get-deploy-url config "projects/" (:code entity)))

(defn project-api-create
  [{:keys [config ds session body]}]
  (let [result (project-logic-create ds session body)]
    (resp/created (get-detail-uri config result))))

(defn project-api-list
  [{:keys [config ds params]}]
  (let [data (project-logic-list ds params)
        result (map (comp
                      (partial update-user-entity-result :owner)
                      (partial list-entity-result get-detail-uri project-attributes config)
                      ) data)]
    (resp/response result)))

(defn project-api-read
  [{:keys [ds params]}]
  (let [result (->> (project-logic-read ds params)
                    (update-user-entity-result :owner))]
    (-> (resp/response (entity-result project-attributes result))
        (etag-header result))))

(defn project-api-update
  [{:keys [config ds body params] :as request}]
  (let [version (get-version request)
        result (project-logic-update ds body params version)]
    (-> response-no-content
        (location-header (get-detail-uri config result)))))

(defn project-api-delete
  [{:keys [ds params] :as request}]
  (let [version (get-version request)]
    (project-logic-delete ds params version)
    response-no-content))
