(ns it.project.read.project-read-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/read/")

(defn- create-request
  [code]
  (-> (mock/request :get (str "/api/projects/" code))
      (auth-header "7b0e6756-d9e4-4001-9d53-000000000001")))

(deftest project-read
  (facts
    "project-read"
    (db-setup prefix "../../users" "setup")
    (let [expected-body (read-json prefix "response")
          request (create-request "code_2")
          response (call-handler request)
          ]
      (verify-response response {:status :ok
                                 :etag   12302
                                 :body   expected-body})
      )))

(deftest project-read-not-found
  (facts
    "project-read-not-found"
    (db-setup prefix "../../users")
    (not-found-test (create-request "not_found"))))
