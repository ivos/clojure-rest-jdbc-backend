(ns it.project.list.project-list-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/list/")

(defn- create-request
  [params]
  (mock/request :get "/projects" params))

(deftest project-list-empty
  (facts
    "project-list-empty"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "empty-response")
          request (create-request {})
          response (call-handler request)
          ]
      (verify-response response {:status :ok
                                 :body   expected-body})
      )))
