(ns it.user.update.user-update-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/update/")

(defn- create-request
  [username version body]
  (-> (mock/request :put (str "/api/users/" username) body)
      (mock/content-type "application/json")
      (if-match-header version)
      (auth-header "7b0e6756-d9e4-4001-9d53-000000000001")))

(defn- ok
  [test-case new-username]
  (db-setup prefix "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        request (create-request "username_2" 123 request-body)
        response (call-handler request)
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

(defn- validation
  [test-case]
  (db-setup prefix "setup")
  (let [request-body (read-json prefix (str test-case "-request"))
        expected-body (read-json prefix (str test-case "-response"))
        request (create-request "username_2" 123 request-body)
        response (call-handler request)
        ]
    (verify-response response {:status :unprocessable-entity
                               :body   expected-body})
    (db-verify prefix "error-verify")
    ))

(deftest user-update-empty
  (facts
    "user-update-empty"
    (validation "empty")))

(deftest user-update-conflict
  (facts
    "user-update-conflict"
    (db-setup prefix "setup")
    (let [request-body (read-json prefix "full-request")
          request (create-request "username_2" 122 request-body)
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "error-verify")
      )))

(deftest user-update-username-duplicate
  (facts
    "user-update-username-duplicate"
    (validation "username-duplicate")))

(deftest user-update-email-duplicate
  (facts
    "user-update-email-duplicate"
    (validation "email-duplicate")))

(deftest user-update-keep-uniques
  (facts
    "user-update-keep-uniques"
    (ok "keep-uniques" "username_2")))
