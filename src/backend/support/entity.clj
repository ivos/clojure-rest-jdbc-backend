(ns backend.support.entity
  (:require [slingshot.slingshot :refer :all]
            [camel-snake-kebab.core :as csk]
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

(defn entity-result
  [attributes entity]
  (verify-found entity)
  (conform-keys entity attributes))

(defn list-entity-result
  [get-detail-uri attributes request entity]
  (-> entity
      (conform-keys attributes)
      (assoc :uri (get-detail-uri request entity))))
