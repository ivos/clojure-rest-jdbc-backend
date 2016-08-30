(ns it.generic.session-touched
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [lightair :refer :all]
            [it.test-support :refer :all]
            [it.project.create.project-create-test :refer [create-request]]
            ))

(def ^:private prefix "generic/")
(def ^:private project-prefix "project/create/")

(deftest session-touched
  (facts
    "session-touched"
    (db-setup project-prefix "../../users")
    (let [request-body (read-json project-prefix "full-request")
          request (create-request request-body)
          response (call-handler request)]
      (println response)
      (db-verify prefix "session-touched-verify"))))
