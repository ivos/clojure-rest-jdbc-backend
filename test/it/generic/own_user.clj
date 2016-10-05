(ns it.generic.own-user
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [lightair :refer :all]
            [it.test-support :refer :all]
            [it.user.read.user-read-test :refer [create-request]]
            ))

(def ^:private prefix "generic/")

(deftest not-own-user
  (facts
    "not-own-user"
    (db-setup prefix "own-user-setup")
    (let [request (create-request "username_3" "7b0e6756-d9e4-4001-9d53-000000000001")
          response (call-handler request)]
      (verify-response response {:status :forbidden
                                 :body   "[\"not.own.user\",\"username_3\",\"username_2\"]"})
      )))

(deftest user-disabled-own-user
  (facts
    "user-disabled-own-user"
    (db-setup prefix "own-user-setup")
    (let [request (create-request "disabled" "7b0e6756-d9e4-4001-9d53-disabled0000")
          response (call-handler request)]
      (verify-response response {:status :unauthorized})
      )))

(deftest session-expired-own-user
  (facts
    "session-expired-own-user"
    (db-setup prefix "own-user-setup")
    (let [request (create-request "expired" "7b0e6756-d9e4-4001-9d53-expired00000")
          response (call-handler request)]
      (verify-response response {:status :unauthorized})
      )))
