(ns it.user.delete.user-delete-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/delete/")

(defn- create-request
  [username version]
  (-> (mock/request :delete (str "/users/" username))
      (if-match-header version)
      (auth-header "7b0e6756-d9e4-4001-9d53-000000000001")))

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
      (db-verify prefix "error-verify")
      )))
