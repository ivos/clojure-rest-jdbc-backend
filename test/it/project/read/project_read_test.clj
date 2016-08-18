(ns it.project.read.project-read-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/read/")

(defn- create-request
  [id]
  (mock/request :get (str "/projects/" id)))

(deftest project-read
  (facts
    "project-read"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "response")
          request (create-request "code-2")
          response (call-handler-at-std-time request)
          ]
      (is-response-ok-version response expected-body 12302)
      )))

(deftest project-read-not-found
  (facts
    "project-read-not-found"
    (db-setup prefix)
    (not-found-test (create-request "non_existent"))))
