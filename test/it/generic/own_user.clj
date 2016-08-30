(ns it.generic.own-user
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [lightair :refer :all]
            [it.test-support :refer :all]
            [it.user.read.user-read-test :refer [create-request]]
            ))

(def ^:private prefix "user/read/")

(deftest not-own-user
  (facts
    "not-own-user"
    (db-setup prefix "setup")
    (let [request (create-request "username_3")
          response (call-handler request)]
      (verify-response response {:status :forbidden
                                 :body   "[\"not.own.user\",\"username_3\",\"username_2\"]"})
      )))
