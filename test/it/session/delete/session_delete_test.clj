(ns it.session.delete.session-delete-test
  (:use midje.sweet)
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "session/delete/")

(defn- create-request
  [token]
  (-> (mock/request :delete (str "/sessions"))
      (auth-header token)))

(deftest session-delete-ok
  (facts
    "session-delete-ok"
    (db-setup prefix "setup")
    (let [request (create-request "7b0e6756-d9e4-4001-9d53-000000000002")
          response (call-handler request)
          ]
      (verify-response response {:status :no-content})
      (db-verify prefix "ok-verify")
      )))
