(ns it.user.read.user-read-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/read/")

(defn- create-request
  [username]
  (mock/request :get (str "/users/" username)))

(deftest user-read
  (facts
    "user-read"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "response")
          request (create-request "username_2")
          response (call-handler request)
          ]
      (verify-response response {:status :ok
                                 :etag   12302
                                 :body   expected-body})
      )))

(deftest user-read-not-found
  (facts
    "user-read-not-found"
    (db-setup prefix)
    (not-found-test (create-request "not_found"))))
