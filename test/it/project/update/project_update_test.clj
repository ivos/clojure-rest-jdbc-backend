(ns it.project.update.project-update-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/update/")

(defn create-request
  [code version body]
  (-> (mock/request :put (str "/projects/" code) body)
      (mock/content-type "application/json")
      (if-match-header version)))

(defn- ok
  [test-case]
  (db-setup prefix "../../users" "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        request (create-request "code_2" 123 request-body)
        response (call-handler request)
        ]
    (verify-response response {:status   :no-content
                               :location "http://localhost:3000/projects/code_2_updated"})
    (db-verify prefix (str test-case "-verify"))
    ))

(deftest project-update-full
  (facts
    "project-update-full"
    (ok "full")))

(deftest project-update-minimal
  (facts
    "project-update-minimal"
    (ok "minimal")))

(deftest project-update-empty
  (facts
    "project-update-empty"
    (db-setup prefix "../../users" "setup")
    (let [request-body (read-json prefix "empty-request")
          expected-body (read-json prefix "empty-response")
          request (create-request "code_2" 123 request-body)
          response (call-handler request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "setup")
      )))

(deftest project-update-conflict
  (facts
    "project-update-conflict"
    (db-setup prefix "../../users" "setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "code_2" 122 request-body)
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "setup")
      )))
