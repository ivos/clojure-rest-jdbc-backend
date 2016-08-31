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
      (if-match-header version)
      (auth-header "7b0e6756-d9e4-4001-9d53-000000000001")))

(defn- ok
  [test-case location]
  (db-setup prefix "../../users" "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        request (create-request "code_2" 123 request-body)
        response (call-handler request)
        ]
    (verify-response response {:status   :no-content
                               :location (str "http://localhost:3000/projects/" location)})
    (db-verify prefix (str test-case "-verify"))
    ))

(deftest project-update-full
  (facts
    "project-update-full"
    (ok "full" "code_2_updated")))

(deftest project-update-minimal
  (facts
    "project-update-minimal"
    (ok "minimal" "code_2_updated")))

(defn- validation
  [test-case]
  (db-setup prefix "../../users" "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        expected-body (read-json prefix (str test-case "-response"))
        request (create-request "code_2" 123 request-body)
        response (call-handler request)
        ]
    (verify-response response {:status :unprocessable-entity
                               :body   expected-body})
    (db-verify prefix "setup")
    ))

(deftest project-update-empty
  (facts
    "project-update-empty"
    (validation "empty")))

(deftest project-update-duplicate
  (facts
    "project-update-duplicate"
    (validation "duplicate")))

(deftest project-update-other
  (facts
    "project-update-other"
    (ok "other" "code_existing_other")))

(deftest project-update-keep-uniques
  (facts
    "project-update-keep-uniques"
    (ok "keep-uniques" "code_2")))

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
