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

(defn- ok
  [test-case code version]
  (db-setup prefix "../../users" "setup")
  (let [expected-body (read-json prefix (str test-case "-response"))
        request (create-request code)
        response (call-handler request)
        ]
    (verify-response response {:status :ok
                               :etag   version
                               :body   expected-body})))

(deftest project-read-full
  (facts
    "project-read-full"
    (ok "full" "code_2" 12303)
    ))

(deftest project-read-minimal
  (facts
    "project-read-minimal"
    (ok "minimal" "code_minimal" 12305)
    ))

(deftest project-read-not-found
  (facts
    "project-read-not-found"
    (db-setup prefix "../../users")
    (not-found-test (create-request "not_found"))))
