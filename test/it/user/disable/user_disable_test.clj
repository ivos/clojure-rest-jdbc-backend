(ns it.user.disable.user-disable-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/disable/")

(defn- create-request
  [username version]
  (-> (mock/request :put (str "/users/" username "/actions/disable"))
      (if-match-header version)))

(defn- ok
  [test-case]
  (db-setup prefix "setup")
  (let [request (create-request "username_2" 123)
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
    (let [request (create-request "username_2" 122)
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "setup")
      )))

(deftest user-disable-disabled
  (facts
    "user-disable-disabled"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "disabled-response")
          request (create-request "username_disabled" 123)
          response (call-handler request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify prefix "setup")
      )))
