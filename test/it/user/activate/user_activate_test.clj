(ns it.user.activate.user-activate-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/activate/")

(defn- create-request
  [username version]
  (-> (mock/request :put (str "/users/" username "/actions/activate"))
      (if-match-header version)))

(defn- ok
  [test-case]
  (db-setup prefix "setup")
  (let [request (create-request "username_2" 123)
        response (call-handler-at-std-time request)
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
    (let [request (create-request "username_2" 122)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "setup")
      )))

(deftest user-activate-active
  (facts
    "user-activate-active"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "active-response")
          request (create-request "username_active" 123)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "setup")
      )))
