(ns it.user.disable.user-disable-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/disable/")

(defn- create-request
  [username version token]
  (-> (mock/request :put (str "/api/users/" username "/actions/disable"))
      (if-match-header version)
      (auth-header token)))

(defn- ok
  [test-case]
  (db-setup prefix "setup")
  (let [request (create-request "username_2" 123 "7b0e6756-d9e4-4001-9d53-000000000001")
        response (call-handler request)
        ]
    (verify-response response {:status   :no-content
                               :location (str "http://localhost:3000/users/username_2")})
    (db-verify prefix (str test-case "-verify"))
    ))

(deftest user-disable-ok
  (facts
    "user-disable-ok"
    (ok "ok")))

(deftest user-disable-conflict
  (facts
    "user-disable-conflict"
    (db-setup prefix "setup")
    (let [request (create-request "username_2" 122 "7b0e6756-d9e4-4001-9d53-000000000001")
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "error-verify")
      )))

(deftest user-disable-disabled
  (facts
    "user-disable-disabled"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "disabled-response")
          request (create-request "username_disabled" 123 "7b0e6756-d9e4-4001-9d53-000000000001")
          response (call-handler request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "error-verify")
      )))
