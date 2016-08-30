(ns it.generic.authorized
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [lightair :refer :all]
            [it.test-support :refer :all]
            [it.session.list.session-list-test :refer [create-request]]
            ))

(def ^:private prefix "generic/")

(deftest other-roles
  (facts
    "other-roles"
    (db-setup prefix "authorized-setup")
    (let [request (create-request "other")
          response (call-handler request)]
      (verify-response response {:status :forbidden
                                 :body   "[\"missing.role\",[\"admin\"],[\"other1\",\"other2\",\"user\"]]"})
      )))

(deftest single-role
  (facts
    "single-role"
    (db-setup prefix "authorized-setup")
    (let [request (create-request "single")
          response (call-handler request)]
      (:status response) => (status-code :ok)
      )))

(deftest multiple-roles
  (facts
    "multiple-roles"
    (db-setup prefix "authorized-setup")
    (let [request (create-request "multiple")
          response (call-handler request)]
      (:status response) => (status-code :ok)
      )))

(deftest disbled-roles
  (facts
    "disbled-roles"
    (db-setup prefix "authorized-setup")
    (let [request (create-request "disabled")
          response (call-handler request)]
      (verify-response response {:status :forbidden
                                 :body   "[\"missing.role\",[\"admin\"],[]]"})
      )))
