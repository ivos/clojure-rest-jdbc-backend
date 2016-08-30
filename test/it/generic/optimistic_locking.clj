(ns it.generic.optimistic-locking
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/update/")

(def ^:private create-request #'it.project.update.project-update-test/create-request)

(defn- perform
  [code version response-code]
  (db-setup prefix "../../users" "setup")
  (let [request-body (read-json prefix "full-request")
        request (create-request code version request-body)
        response (call-handler request)
        ]
    (verify-response response {:status response-code})
    (db-verify prefix "setup")
    ))

(deftest optimistic-locking-conflict
  (facts
    "optimistic-locking-conflict"
    (perform "code_2" 122 :precondition-failed)))

(deftest optimistic-locking-not-found
  (facts
    "optimistic-locking-not-found"
    (perform "not_found" 123 :precondition-failed)))

(deftest optimistic-locking-no-version
  (facts
    "optimistic-locking-no-version"
    (perform "code_2" nil :precondition-required)))

(deftest optimistic-locking-invalid-version
  (facts
    "optimistic-locking-invalid-version"
    (perform "code_2" "invalid_version" :precondition-failed)))
