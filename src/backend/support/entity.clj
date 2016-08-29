(ns backend.support.entity
  (:require [slingshot.slingshot :refer [throw+]]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [backend.support.ring :refer :all]))

(defn verify-found
  [entity]
  (when (nil? entity)
    (throw+ {:type     :custom-response
             :response {:status (status-code :not-found)
                        :body   {:code :entity.not.found}}})))

(defn entity-listed
  [entity]
  (transform-keys csk/->camelCase entity))

(defn entity-read
  [entity]
  (verify-found entity)
  (entity-listed entity))

(defn expand-entity
  [tc expand-db-fn rel-attribute entity]
  (let [id (get entity rel-attribute)
        related (first (expand-db-fn tc {:ids [id]}))]
    (assoc entity rel-attribute related)))

(defn expand-list
  [tc expand-db-fn rel-attribute id-attribute data]
  (let [get-id #(get %1 rel-attribute)
        ids (distinct (map get-id data))
        entities (expand-db-fn tc {:ids ids})
        find-entity (fn [id]
                      (some
                        #(when (= id (get %1 id-attribute)) %1)
                        entities))
        expand-one #(update %1 rel-attribute (comp entity-listed find-entity))]
    (map expand-one data)))
