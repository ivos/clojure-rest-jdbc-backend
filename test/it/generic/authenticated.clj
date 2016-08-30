(ns it.generic.authenticated
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "generic/")
(def ^:private session-prefix "session/delete/")

(def ^:private create-request #'it.session.delete.session-delete-test/create-request)

(defn- perform
  [token]
  (db-setup session-prefix "setup")
  (let [expected-body (read-json prefix "no-valid-session-response")
        request (create-request token)
        response (call-handler request)
        ]
    (verify-response response {:status :unauthorized
                               :body   expected-body})
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
