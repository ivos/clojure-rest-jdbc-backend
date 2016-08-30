(ns it.generic.data-types
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [lightair :refer :all]
            [it.test-support :refer :all]
            [it.project.update.project-update-test :refer [create-request]]
            ))

(def ^:private prefix "generic/")
(def ^:private project-prefix "project/update/")

(deftest data-types-invalid
  (facts
    "data-types-invalid"
    (db-setup project-prefix "../../users" "setup")
    (let [request-body (read-json prefix "invalid-request")
          expected-body (read-json prefix "invalid-response")
          request (create-request "code_2" 123 request-body)
          response (call-handler request)
          ]
      (verify-response response {:status :unprocessable-entity
                                 :body   expected-body})
      (db-verify project-prefix "setup")
      )))
