(ns backend.app.user.user-api
  (:require [ring.util.response :as resp]
            [backend.support.ring :refer :all]
            [backend.support.api :refer :all]
            [backend.support.util :refer :all]
            [backend.app.user.user-logic :refer :all]
            ))

(defn update-user-entity-result
  [rel-attribute entity]
  (update entity rel-attribute
          (comp (partial entity-result user-attributes)
                filter-password)))

(defn- get-detail-uri
  [request entity]
  (get-deploy-url request "users/" (:username entity)))

(defn user-api-create
  [{:keys [config ds body]}]
  (let [result (user-logic-create ds body)]
    (resp/created (get-detail-uri config result))))

(defn user-api-list
  [{:keys [config ds params]}]
  (let [data (->> (user-logic-list ds params)
                  (map filter-password))
        result (map (partial list-entity-result get-detail-uri user-attributes config) data)]
    (resp/response result)))

(defn user-api-read
  [{:keys [ds params]}]
  (let [result (->> (user-logic-read ds params)
                    filter-password)]
    (-> (resp/response (entity-result user-attributes result))
        (etag-header result))))

(defn user-api-update
  [{:keys [config ds body params] :as request}]
  (let [version (get-version request)
        result (user-logic-update ds body params version)]
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
  (perform-action request user-logic-disable))

(defn user-api-activate
  [request]
  (perform-action request user-logic-activate))

(defn user-api-delete
  [{:keys [ds params] :as request}]
  (let [version (get-version request)]
    (user-logic-delete ds params version)
    response-no-content))
