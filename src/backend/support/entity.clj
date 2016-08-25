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

(defn entity-read
  [entity]
  (verify-found entity)
  (transform-keys csk/->camelCase entity))

(defn entity-listed
  [entity]
  (transform-keys csk/->camelCase entity))
