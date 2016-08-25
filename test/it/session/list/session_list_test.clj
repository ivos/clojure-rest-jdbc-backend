(ns it.session.list.session-list-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [clj-time.core :as t]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "session/list/")

(defn- create-request
  []
  (mock/request :get "/sessions"))

(deftest session-list-empty
  (facts
    "session-list-empty"
    (t/do-at
      std-time

      (db-setup prefix "setup")
      (let [expected-body (read-json prefix "empty-response")
            request (create-request)
            response (call-handler request)
            ]
        (verify-response response {:status :ok
                                   :body   expected-body})))))
