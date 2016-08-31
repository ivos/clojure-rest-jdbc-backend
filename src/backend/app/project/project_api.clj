(ns backend.app.project.project-api
  (:refer-clojure :exclude [list read update])
  (:require [ring.util.response :as resp]
            [backend.support.ring :refer [get-deploy-url etag-header location-header get-version response-no-content]]
            [backend.support.api :as api]
            [backend.app.project.project-logic :as logic]
            [backend.app.user.user-api :as user]
            ))

(defn- get-detail-uri
  [config entity]
  (get-deploy-url config "projects/" (:code entity)))

(defn create
  [{:keys [config ds session body]}]
  (let [result (logic/create ds session body)]
    (resp/created (get-detail-uri config result))))

(defn list
  [{:keys [config ds params]}]
  (let [data (logic/list ds params)
        result (map (comp
                      (partial user/update-entity-result :owner)
                      (partial api/list-entity-result get-detail-uri logic/attributes config)
                      ) data)]
    (resp/response result)))

(defn read
  [{:keys [ds session params]}]
  (let [result (->> (logic/read ds session params)
                    (user/update-entity-result :owner))]
    (-> (resp/response (api/entity-result logic/attributes result))
        (etag-header result))))

(defn update
  [{:keys [config ds session body params] :as request}]
  (let [version (get-version request)
        result (logic/update ds session body params version)]
    (-> response-no-content
        (location-header (get-detail-uri config result)))))

(defn delete
  [{:keys [ds session params] :as request}]
  (let [version (get-version request)]
    (logic/delete ds session params version)
    response-no-content))
