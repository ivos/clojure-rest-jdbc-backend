(ns backend.support.entity
  (:require [slingshot.slingshot :refer :all]
            [backend.support.ring :refer :all]))

(defn- verify-found
  [entity]
  (if (nil? entity)
    (throw+ {:type     :custom-response
             :response {:status (status-code :not-found)
                        :body   {:code :entity.not.found}}})
    entity))

(defn entity-result
  [entity]
  (-> entity
      verify-found
      (dissoc :id :version)))

(defn list-entity-result
  [get-detail-uri request entity]
  (-> entity
      (assoc :uri (get-detail-uri request entity))
      (dissoc :id :version)))
