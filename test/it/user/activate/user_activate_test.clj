(ns it.user.activate.user-activate-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/activate/")

(defn- create-request
  [username version token]
  (-> (mock/request :put (str "/api/users/" username "/actions/activate"))
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

(deftest user-activate-ok
  (facts
    "user-activate-ok"
    (ok "ok")))

(deftest user-activate-conflict
  (facts
    "user-activate-conflict"
    (db-setup prefix "setup")
    (let [request (create-request "username_2" 122 "7b0e6756-d9e4-4001-9d53-000000000001")
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "error-verify")
      )))

(deftest user-activate-active
  (facts
    "user-activate-active"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "active-response")
          request (create-request "username_active" 123 "7b0e6756-d9e4-4001-9d53-000000000001")
          response (call-handler request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "error-verify")
      )))
