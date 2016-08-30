(ns it.generic.optimistic-locking
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/update/")

(def ^:private create-request #'it.project.update.project-update-test/create-request)

(deftest optimistic-locking-conflict
  (facts
    "optimistic-locking-conflict"
    (db-setup prefix "setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "code_2" 122 request-body)
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "setup")
      )))

(deftest optimistic-locking-not-found
  (facts
    "optimistic-locking-not-found"
    (db-setup prefix "setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "not_found" 123 request-body)
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "setup")
      )))

(deftest optimistic-locking-no-version
  (facts
    "optimistic-locking-no-version"
    (db-setup prefix "setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "code_2" nil request-body)
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-required})
      (db-verify prefix "setup")
      )))

(deftest optimistic-locking-invalid-version
  (facts
    "optimistic-locking-invalid-version"
    (db-setup prefix "setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "code_2" "invalid_version" request-body)
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "setup")
      )))
