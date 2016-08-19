(ns backend.support.entity
  (:require [slingshot.slingshot :refer :all]
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
        values (map #(get entity %1) keys)]
    (apply array-map (interleave keys values))))

(defn entity-result
  [attributes entity]
  (verify-found entity)
  (conform-keys entity attributes))

(defn list-entity-result
  [get-detail-uri attributes request entity]
  (-> entity
      (conform-keys attributes)
      (assoc :uri (get-detail-uri request entity))))
