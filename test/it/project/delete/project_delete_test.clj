(ns it.project.delete.project-delete-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/delete/")

(defn- create-request
  [code version]
  (-> (mock/request :delete (str "/projects/" code))
      (if-match-header version)))

(deftest project-delete-ok
  (facts
    "project-delete-ok"
    (db-setup prefix "../../users" "setup")
    (let [request (create-request "code_2" 123)
          response (call-handler request)
          ]
      (verify-response response {:status :no-content})
      (db-verify prefix "ok-verify")
      )))

(deftest project-delete-conflict
  (facts
    "project-delete-conflict"
    (db-setup prefix "../../users" "setup")
    (let [request (create-request "code_2" 122)
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "setup")
      )))
