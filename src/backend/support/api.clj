(ns backend.support.api
  (:require [clj-time.coerce :as tc]
            [flatland.ordered.map :refer [ordered-map]]
            [backend.support.entity :refer :all]))

(defn- remove-inbound-keys
  [attributes entity]
  (let [out-keys-filter #(let [v (get attributes %1)]
                          (or (nil? v)
                              (nil? (:direction v))
                              (not= :in (:direction v))))
        out-keys (filter out-keys-filter (keys attributes))
        values (map #(get entity %1) out-keys)]
    (apply ordered-map (interleave out-keys values))))

(defn- format-data-types
  [attributes entity]
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
  (->> entity
       (remove-inbound-keys attributes)
       (format-data-types attributes)))

(defn list-entity-result
  [get-detail-uri attributes config entity]
  (assoc
    (->> entity
         (remove-inbound-keys attributes)
         (format-data-types attributes))
    :uri (get-detail-uri config entity)))
