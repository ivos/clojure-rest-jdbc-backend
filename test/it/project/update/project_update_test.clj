(ns it.project.update.project-update-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/update/")

(defn- create-request
  [id version body]
  (-> (mock/request :put (str "/projects/" id) body)
      (mock/content-type "application/json")
      (if-match-header version)))

(deftest project-update-full
  (facts
    "project-update-full"
    (db-setup prefix "full-setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "code_2" 123 request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status   :no-content
                                 :etag     124
                                 :location "http://localhost:3000/projects/code_2_updated"})
      (db-verify prefix "full-verify")
      )))

(deftest project-update-empty
  (facts
    "project-update-empty"
    (db-setup prefix "full-setup")
    (let [request-body (read-json prefix "empty-request")
          expected-body (read-json prefix "empty-response")
          request (create-request "code_2" 123 request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "full-setup")
      )))

(deftest project-update-not-found
  (facts
    "project-update-not-found"
    (db-setup prefix "full-setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "not_found" 123 request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "full-setup")
      )))

(deftest project-update-conflict
  (facts
    "project-update-conflict"
    (db-setup prefix "full-setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "code_2" 122 request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "full-setup")
      )))

(deftest project-update-no-version
  (facts
    "project-update-no-version"
    (db-setup prefix "full-setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "code_2" nil request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :precondition-required})
      (db-verify prefix "full-setup")
      )))
