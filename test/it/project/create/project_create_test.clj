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
      (mock/content-type "application/json")))

(deftest project-create-full
  (facts
    "project-create-full"
    (db-setup prefix)
    (let [request-body (read-json prefix "full-request")
          expected-body (read-json prefix "full-response")
          request (create-request request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :created
                                 :etag   1
                                 :location "http://localhost:3000/projects/code_1"
                                 :body   expected-body})
      (db-verify prefix "full-verify")
      )))

(deftest project-create-empty
  (facts
    "project-create-empty"
    (db-setup prefix)
    (let [request-body (read-json prefix "empty-request")
          expected-body (read-json prefix "empty-response")
          request (create-request request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "empty-verify")
      )))
