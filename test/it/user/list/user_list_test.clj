(ns it.user.list.user-list-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/list/")

(defn- create-request
  [params]
  (mock/request :get "/users" params))

(deftest user-list-empty
  (facts
    "user-list-empty"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "empty-response")
          request (create-request {})
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :ok
                                 :body   expected-body})
      )))
