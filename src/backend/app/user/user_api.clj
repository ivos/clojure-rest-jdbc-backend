(ns backend.app.user.user-api
  (:require [ring.util.response :as resp]
            [backend.support.ring :refer [get-deploy-url etag-header location-header get-version response-no-content]]
            [backend.support.api :as api]
            [backend.support.util :as util]
            [backend.app.user.user-logic :as logic]
            ))

(defn update-user-entity-result
  [rel-attribute entity]
  (update entity rel-attribute
          (comp (partial api/entity-result logic/user-attributes)
                util/filter-password)))

(defn- get-detail-uri
  [request entity]
  (get-deploy-url request "users/" (:username entity)))

(defn user-api-create
  [{:keys [config ds body]}]
  (let [result (logic/user-logic-create ds body)]
    (resp/created (get-detail-uri config result))))

(defn user-api-list
  [{:keys [config ds params]}]
  (let [data (->> (logic/user-logic-list ds params)
                  (map util/filter-password))
        result (map (partial api/list-entity-result get-detail-uri logic/user-attributes config) data)]
    (resp/response result)))

(defn user-api-read
  [{:keys [ds params]}]
  (let [result (->> (logic/user-logic-read ds params)
                    util/filter-password)]
    (-> (resp/response (api/entity-result logic/user-attributes result))
        (etag-header result))))

(defn user-api-update
  [{:keys [config ds body params] :as request}]
  (let [version (get-version request)
        result (logic/user-logic-update ds body params version)]
    (-> response-no-content
        (location-header (get-detail-uri config result)))))

(defn- perform-action
  [{:keys [config ds params] :as request} logic-fn]
  (let [version (get-version request)]
    (logic-fn ds params version)
    (-> response-no-content
        (location-header (get-detail-uri config params)))))

(defn user-api-disable
  [request]
  (perform-action request logic/user-logic-disable))

(defn user-api-activate
  [request]
  (perform-action request logic/user-logic-activate))

(defn user-api-delete
  [{:keys [ds params] :as request}]
  (let [version (get-version request)]
    (logic/user-logic-delete ds params version)
    response-no-content))
