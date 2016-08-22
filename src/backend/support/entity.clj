(ns backend.support.entity
  (:require [slingshot.slingshot :refer :all]
            [camel-snake-kebab.core :as csk]
            [clj-time.coerce :as tc]
            [flatland.ordered.map :refer [ordered-map]]
            [backend.support.ring :refer :all]))

(defn- verify-found
  [entity]
  (when (nil? entity)
    (throw+ {:type     :custom-response
             :response {:status (status-code :not-found)
                        :body   {:code :entity.not.found}}})))

(defn- conform-keys
  [entity attributes]
  (let [keys (keys attributes)
        values (map #(get entity (csk/->snake_case %1)) keys)]
    (apply ordered-map (interleave keys values))))

(defn- format-data-types
  [entity attributes]
  (let [keys (keys entity)
        format-attribute #(let [value (get entity %1)]
                           (if-let [data-type (get-in attributes [%1 :type])]
                             (case data-type
                               (:date :time) (str value)
                               :timestamp (-> value tc/to-date-time str)
                               (:integer :number) value)
                             value))
        values (map format-attribute keys)]
    (apply ordered-map (interleave keys values))))

(defn entity-result
  [attributes entity]
  (verify-found entity)
  (-> entity
      (conform-keys attributes)
      (format-data-types attributes)))

(defn list-entity-result
  [get-detail-uri attributes request entity]
  (-> entity
      (conform-keys attributes)
      (format-data-types attributes)
      (assoc :uri (get-detail-uri request entity))))
