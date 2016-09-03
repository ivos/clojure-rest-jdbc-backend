(ns it.session.delete.session-delete-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "session/delete/")

(defn create-request
  [token]
  (-> (mock/request :delete (str "/api/sessions"))
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
