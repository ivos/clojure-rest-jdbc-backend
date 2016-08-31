(ns backend.app.user.user-api
  (:refer-clojure :exclude [list read update])
  (:require [clojure.core :as core]
            [ring.util.response :as resp]
            [backend.support.ring :refer [get-deploy-url etag-header location-header get-version response-no-content]]
            [backend.support.api :as api]
            [backend.support.util :as util]
            [backend.app.user.user-logic :as logic]
            ))

(defn update-entity-result
  [rel-attribute entity]
  (core/update entity rel-attribute
               (comp (partial api/entity-result logic/attributes)
                     util/filter-password)))

(defn- get-detail-uri
  [request entity]
  (get-deploy-url request "users/" (:username entity)))

(defn create
  [{:keys [config ds body]}]
  (let [result (logic/create ds body)]
    (resp/created (get-detail-uri config result))))

(defn list
  [{:keys [config ds params]}]
  (let [data (->> (logic/list ds params)
                  (map util/filter-password))
        result (map (partial api/list-entity-result get-detail-uri logic/attributes config) data)]
    (resp/response result)))

(defn read
  [{:keys [ds params]}]
  (let [result (->> (logic/read ds params)
                    util/filter-password)]
    (-> (resp/response (api/entity-result logic/attributes result))
        (etag-header result))))

(defn update
  [{:keys [config ds body params] :as request}]
  (let [version (get-version request)
        result (logic/update ds body params version)]
    (-> response-no-content
        (location-header (get-detail-uri config result)))))

(defn- perform-action
  [{:keys [config ds params] :as request} logic-fn]
  (let [version (get-version request)]
    (logic-fn ds params version)
    (-> response-no-content
        (location-header (get-detail-uri config params)))))

(defn disable
  [request]
  (perform-action request logic/disable))

(defn activate
  [request]
  (perform-action request logic/activate))

(defn delete
  [{:keys [ds params] :as request}]
  (let [version (get-version request)]
    (logic/delete ds params version)
    response-no-content))
