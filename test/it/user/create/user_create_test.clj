(ns it.user.create.user-create-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/create/")

(defn- create-request
  [body]
  (-> (mock/request :post "/users" body)
      (mock/content-type "application/json")))

(defn- ok
  [test-case]
  (db-setup prefix)
  (let [request-body (read-json prefix (str test-case "-request"))
        request (create-request request-body)
        response (call-handler-at-std-time request)
        ]
    (verify-response response {:status   :created
                               :location "http://localhost:3000/users/username_1"})
    (db-verify prefix (str test-case "-verify"))
    ))

(deftest user-create-full
  (facts
    "user-create-full"
    (ok "full")))

(deftest user-create-empty
  (facts
    "user-create-empty"
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