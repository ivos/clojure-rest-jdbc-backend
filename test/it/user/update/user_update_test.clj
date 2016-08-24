(ns it.user.update.user-update-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/update/")

(defn- create-request
  [username version body]
  (-> (mock/request :put (str "/users/" username) body)
      (mock/content-type "application/json")
      (if-match-header version)))

(defn- ok
  [test-case new-username]
  (db-setup prefix "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        request (create-request "username_2" 123 request-body)
        response (call-handler-at-std-time request)
        ]
    (verify-response response {:status   :no-content
                               :location (str "http://localhost:3000/users/" new-username)})
    (db-verify prefix (str test-case "-verify"))
    ))

(deftest user-update-full
  (facts
    "user-update-full"
    (ok "full" "username_2_updated")))

(deftest user-update-minimal
  (facts
    "user-update-minimal"
    (ok "minimal" "username_2_updated")))

(deftest user-update-empty
  (facts
    "user-update-empty"
    (db-setup prefix "setup")
    (let [request-body (read-json prefix "empty-request")
          expected-body (read-json prefix "empty-response")
          request (create-request "username_2" 123 request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "setup")
      )))

(deftest user-update-conflict
  (facts
    "user-update-conflict"
    (db-setup prefix "setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "username_2" 122 request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "setup")
      )))

(deftest user-update-username-duplicate
  (facts
    "user-update-username-duplicate"
    (db-setup prefix "setup")
    (let [request-body (read-json prefix "username-duplicate-request")
          expected-body (read-json prefix "username-duplicate-response")
          request (create-request "username_2" 123 request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "setup")
      )))

(deftest user-update-email-duplicate
  (facts
    "user-update-email-duplicate"
    (db-setup prefix "setup")
    (let [request-body (read-json prefix "email-duplicate-request")
          expected-body (read-json prefix "email-duplicate-response")
          request (create-request "username_2" 123 request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "setup")
      )))

(deftest user-update-keep-uniques
  (facts
    "user-update-keep-uniques"
    (ok "keep-uniques" "username_2")))
