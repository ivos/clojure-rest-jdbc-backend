(ns it.generic.data-types
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "generic/")
(def ^:private project-prefix "project/update/")

(defn- create-request
  [id version body]
  (#'it.project.update.project-update-test/create-request id version body))

(deftest data-types-invalid
  (facts
    "data-types-invalid"
    (db-setup project-prefix "setup")
    (let [request-body (read-json prefix "invalid-request")
          expected-body (read-json prefix "invalid-response")
          request (create-request "code_2" 123 request-body)
          response (call-handler-at-std-time request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify project-prefix "setup")
      )))
