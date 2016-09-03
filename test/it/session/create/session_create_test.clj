(ns it.session.create.session-create-test
  (:require [clojure.string :as string]
            [clojure.test :refer [deftest]]
            [backend.support.ring :refer :all]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [clj-time.core :as t]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "session/create/")

(defn- create-request
  [body]
  (-> (mock/request :post "/api/sessions" body)
      (mock/content-type "application/json")))

(defn- ok
  [test-case]
  (t/do-at
    std-time

    (db-setup prefix "setup")
    (let [request-body (read-json prefix (str test-case "-request"))
          expected-body (read-json prefix (str test-case "-response"))
          request (create-request request-body)
          response (call-handler request)
          response (update response :body string/replace #"\"token\":\"[0-9a-f\-]{36}\"" "\"token\":\"REPLACED\"")
          ]
      (verify-response response {:status :created
                                 :body   expected-body})
      (db-verify prefix (str test-case "-verify"))
      )))

(deftest session-create-full
  (facts
    "session-create-full"
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

(deftest session-create-empty
  (facts
    "session-create-empty"
    (validation "empty")))

(deftest session-create-user-not-found
  (facts
    "session-create-user-not-found"
    (db-setup prefix)
    (let [request-body (read-json prefix "user-not-found-request")]
      (not-found-test (create-request request-body)))))

(deftest session-create-user-disabled
  (facts
    "session-create-user-disabled"
    (validation "user-disabled")))

(deftest session-create-password-invalid
  (facts
    "session-create-password-invalid"
    (validation "password-invalid")))
