(ns it.project.delete.project-delete-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/delete/")

(defn- create-request
  [code version]
  (-> (mock/request :delete (str "/projects/" code))
      (if-match-header version)))

(deftest project-delete-full
  (facts
    "project-delete-full"
    (db-setup prefix "full-setup")
    (let [request (create-request "code_2" 123)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :no-content})
      (db-verify prefix "full-verify")
      )))

(deftest project-delete-conflict
  (facts
    "project-delete-conflict"
    (db-setup prefix "full-setup")
    (let [request (create-request "code_2" 122)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "full-setup")
      )))
