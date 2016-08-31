(ns backend.app.project.project-api
  (:require [ring.util.response :as resp]
            [backend.support.ring :refer [get-deploy-url etag-header location-header get-version response-no-content]]
            [backend.support.api :as api]
            [backend.app.project.project-logic :as logic]
            [backend.app.user.user-api :as user]
            ))

(defn- get-detail-uri
  [config entity]
  (get-deploy-url config "projects/" (:code entity)))

(defn project-api-create
  [{:keys [config ds session body]}]
  (let [result (logic/project-logic-create ds session body)]
    (resp/created (get-detail-uri config result))))

(defn project-api-list
  [{:keys [config ds params]}]
  (let [data (logic/project-logic-list ds params)
        result (map (comp
                      (partial user/update-user-entity-result :owner)
                      (partial api/list-entity-result get-detail-uri logic/project-attributes config)
                      ) data)]
    (resp/response result)))

(defn project-api-read
  [{:keys [ds session params]}]
  (let [result (->> (logic/project-logic-read ds session params)
                    (user/update-user-entity-result :owner))]
    (-> (resp/response (api/entity-result logic/project-attributes result))
        (etag-header result))))

(defn project-api-update
  [{:keys [config ds session body params] :as request}]
  (let [version (get-version request)
        result (logic/project-logic-update ds session body params version)]
    (-> response-no-content
        (location-header (get-detail-uri config result)))))

(defn project-api-delete
  [{:keys [ds session params] :as request}]
  (let [version (get-version request)]
    (logic/project-logic-delete ds session params version)
    response-no-content))
