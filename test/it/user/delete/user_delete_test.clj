(ns it.user.delete.user-delete-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/delete/")

(defn- create-request
  [username version]
  (-> (mock/request :delete (str "/users/" username))
      (if-match-header version)))

(deftest user-delete-ok
  (facts
    "user-delete-ok"
    (db-setup prefix "setup")
    (let [request (create-request "username_2" 123)
          response (call-handler request)
          ]
      (verify-response response {:status :no-content})
      (db-verify prefix "ok-verify")
      )))

(deftest user-delete-conflict
  (facts
    "user-delete-conflict"
    (db-setup prefix "setup")
    (let [request (create-request "username_2" 122)
          response (call-handler request)
          ]
      (verify-response response {:status :precondition-failed})
      (db-verify prefix "setup")
      )))
