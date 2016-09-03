(ns it.user.create.user-create-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/create/")

(defn- create-request
  [body]
  (-> (mock/request :post "/api/users" body)
      (mock/content-type "application/json")))

(defn- ok
  [test-case]
  (db-setup prefix "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        request (create-request request-body)
        response (call-handler request)
        ]
    (verify-response response {:status   :created
                               :location "http://localhost:3000/users/username_1"})
    (db-verify prefix (str test-case "-verify"))
    ))

(deftest user-create-full
  (facts
    "user-create-full"
    (ok "full")))

(defn- validation
  [test-case]
  (db-setup prefix "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        expected-body (read-json prefix (str test-case "-response"))
        request (create-request request-body)
        response (call-handler request)
        ]
    (verify-response response {:status :unprocessable-entity
                               :body   expected-body})
    (db-verify prefix "setup")
    ))

(deftest user-create-empty
  (facts
    "user-create-empty"
    (validation "empty")))

(deftest user-create-username-duplicate
  (facts
    "user-create-username-duplicate"
    (validation "username-duplicate")))

(deftest user-create-email-duplicate
  (facts
    "user-create-email-duplicate"
    (validation "email-duplicate")))
