(ns backend.support.util
  (:require [buddy.hashers :as hashers]))

(defn filter-password
  [entity]
  (if (:password entity)
    (assoc entity :password "*****")
    entity))

(defn- hash-password
  [password]
  (hashers/derive password))

(defn hash-entity
  [entity]
  (let [password (:password entity)
        hashed (if password
                 (assoc entity :passwordHash (hash-password password))
                 entity)]
    (dissoc hashed :password)))
