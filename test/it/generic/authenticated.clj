(ns it.generic.authenticated
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [lightair :refer :all]
            [it.test-support :refer :all]
            [it.session.delete.session-delete-test :refer [create-request]]
            ))

(def ^:private prefix "generic/")
(def ^:private session-prefix "session/delete/")

(defn- perform
  [token]
  (db-setup session-prefix "setup")
  (let [request (create-request token)
        response (call-handler request)]
    (verify-response response {:status :unauthorized})
    (db-verify session-prefix "setup")
    ))

(deftest token-not-present
  (facts
    "token-not-present"
    (perform nil)))

(deftest token-not-found
  (facts
    "token-not-found"
    (perform "non-existent")))

(deftest session-expired
  (facts
    "session-expired"
    (perform "expired")))

(deftest user-disabled
  (facts
    "user-disabled"
    (perform "disabled")))
