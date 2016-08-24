(ns backend.logic.user
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as db]
            [ring.util.response :as resp]
            [hugsql.core :refer [def-db-fns]]
            [flatland.ordered.map :refer [ordered-map]]
            [backend.support.repo :as repo]
            [backend.support.ring :refer :all]
            [backend.support.entity :refer :all]
            [backend.support.validation :refer :all]
            [backend.support.util :refer [filter-password]]
            )
  (:import (java.security MessageDigest)
           (org.apache.commons.codec.binary Hex)))

(def email-pattern
  #"^[_A-Za-z0-9-\+]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$")

(def ^:private attributes
  (ordered-map
    :username {:required   true
               :max-length 100
               :pattern    #"[a-z0-9_]*"}
    :email {:required   true
            :max-length 100
            :pattern    email-pattern}
    :name {:required   true
           :max-length 100}
    :password {:max-length 100
               :direction  :in}
    :status {:direction :out}
    ))

(defn- get-detail-uri
  [request entity]
  (get-deploy-url request "users/" (:username entity)))

(def-db-fns "backend/logic/user.sql")

(defn- hash-password
  [password]
  (let [bytes (.getBytes ^String password)
        digest (-> (MessageDigest/getInstance "SHA-256")
                   (.digest bytes))
        hash (Hex/encodeHexString digest)]
    hash))

(defn- hash-entity
  [entity]
  (let [password (:password entity)
        hashed (if password
                 (assoc entity :passwordHash (hash-password password))
                 entity)]
    (dissoc hashed :password)))

(defn- validate-unique-username-on-create
  [tc entity]
  (when (read-user tc (select-keys entity [:username]))
    (validation-failure {:username [[:duplicate]]})))

(defn- validate-unique-email-on-create
  [tc entity]
  (when (read-user tc {:username (:email entity)})
    (validation-failure {:email [[:duplicate]]})))

(defn user-create
  [{:keys [ds body] :as request}]
  (log/debug "Creating user" (filter-password body))
  (let [attributes (assoc-in attributes [:password :required] true)
        entity (-> (valid attributes body)
                   hash-entity
                   (assoc :status "active"))]
    (db/with-db-transaction
      [tc ds]
      (validate-unique-username-on-create tc entity)
      (validate-unique-email-on-create tc entity)
      (let [result (repo/create! tc :user entity)
            response (resp/created (get-detail-uri request result))]
        (log/debug "Created user" (filter-password result))
        response))))

(defn user-list
  [{:keys [ds params] :as request}]
  (log/debug "Listing users" params)
  (db/with-db-transaction
    [tc ds]
    (let [data (->> (list-all-users tc)
                    (map filter-password))
          result (map (partial list-entity-result get-detail-uri attributes request) data)
          response (resp/response result)]
      (log/debug "Listed users" data)
      response)))

(defn user-read
  [{:keys [ds params]}]
  (log/debug "Reading user" params)
  (db/with-db-transaction
    [tc ds]
    (let [result (-> (read-user tc params)
                     filter-password)
          response (-> (resp/response (entity-result attributes result))
                       (etag-header result))]
      (log/debug "Read user" result)
      response)))

(defn- validate-unique-username-on-update
  [tc entity where]
  (when (and (not= (:username entity) (:username where))
             (read-user tc (select-keys entity [:username])))
    (validation-failure {:username [[:duplicate]]})))

(defn- validate-unique-email-on-update
  [tc entity where]
  (when-let [found (read-user tc {:username (:email entity)})]
    (when (not= (:username found) (:username where))
      (validation-failure {:email [[:duplicate]]}))))

(defn user-update
  [{:keys [ds body params] :as request}]
  (let [version (get-version request)
        where (assoc params :version version)]
    (log/debug "Updating user" (filter-password body) "where" where)
    (let [entity (-> (valid attributes body)
                     hash-entity
                     (assoc :version version))]
      (db/with-db-transaction
        [tc ds]
        (validate-unique-username-on-update tc entity where)
        (validate-unique-email-on-update tc entity where)
        (let [result (repo/update! tc :user entity where)
              response (-> response-no-content
                           (location-header (get-detail-uri request result)))]
          (log/debug "Updated user" (filter-password result))
          response)))))

(defn user-delete
  [{:keys [ds params] :as request}]
  (let [version (get-version request)
        where (assoc params :version version)]
    (log/debug "Deleting user where" where)
    (db/with-db-transaction
      [tc ds]
      (repo/delete! tc :user where)
      (log/debug "Deleted user where" where)
      response-no-content)))
