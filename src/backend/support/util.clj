(ns backend.support.util
  (:import (org.apache.commons.codec.binary Hex)
           (java.security MessageDigest)))

(defn filter-password
  [entity]
  (if (:password entity)
    (assoc entity :password "*****")
    entity))

(defn- hash-password
  [password]
  (let [bytes (.getBytes ^String password)
        digest (-> (MessageDigest/getInstance "SHA-256")
                   (.digest bytes))
        hash (Hex/encodeHexString digest)]
    hash))

(defn hash-entity
  [entity]
  (let [password (:password entity)
        hashed (if password
                 (assoc entity :passwordHash (hash-password password))
                 entity)]
    (dissoc hashed :password)))
