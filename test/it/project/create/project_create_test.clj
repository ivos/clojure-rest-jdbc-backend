(ns it.project.create.project-create-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/create/")

(defn- create-request
  [body]
  (-> (mock/request :post "/projects" body)
      (mock/content-type "application/json")
      (auth-header "7b0e6756-d9e4-4001-9d53-000000000001")))

(defn- ok
  [test-case]
  (db-setup prefix "../../users")
  (let [request-body (read-json prefix (str test-case "-request"))
        request (create-request request-body)
        response (call-handler request)
        ]
    (verify-response response {:status   :created
                               :location "http://localhost:3000/projects/code_1"})
    (db-verify prefix (str test-case "-verify"))
    ))

(deftest project-create-full
  (facts
    "project-create-full"
    (ok "full")))

(deftest project-create-minimal
  (facts
    "project-create-minimal"
    (ok "minimal")))

(deftest project-create-empty
  (facts
    "project-create-empty"
    (db-setup prefix "../../users")
    (let [request-body (read-json prefix "empty-request")
          expected-body (read-json prefix "empty-response")
          request (create-request request-body)
          response (call-handler request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "empty-verify")
      )))
