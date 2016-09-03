(ns it.project.create.project-create-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/create/")

(defn create-request
  [body]
  (-> (mock/request :post "/api/projects" body)
      (mock/content-type "application/json")
      (auth-header "7b0e6756-d9e4-4001-9d53-000000000001")))

(defn- ok
  [test-case location]
  (db-setup prefix "../../users" "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        request (create-request request-body)
        response (call-handler request)
        ]
    (verify-response response {:status   :created
                               :location (str "http://localhost:3000/projects/" location)})
    (db-verify prefix (str test-case "-verify"))
    ))

(deftest project-create-full
  (facts
    "project-create-full"
    (ok "full" "code_1")))

(deftest project-create-minimal
  (facts
    "project-create-minimal"
    (ok "minimal" "code_1")))

(defn- validation
  [test-case]
  (db-setup prefix "../../users" "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        expected-body (read-json prefix (str test-case "-response"))
        request (create-request request-body)
        response (call-handler request)
        ]
    (verify-response response {:status :unprocessable-entity
                               :body   expected-body})
    (db-verify prefix "error-verify")
    ))

(deftest project-create-empty
  (facts
    "project-create-empty"
    (validation "empty")))

(deftest project-create-duplicate
  (facts
    "project-create-duplicate"
    (validation "duplicate")))

(deftest project-create-other
  "Same code as some other user has."
  (facts
    "project-create-other"
    (ok "other" "code_existing_other")))
