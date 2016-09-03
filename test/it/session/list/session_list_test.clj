(ns it.session.list.session-list-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [clj-time.core :as t]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "session/list/")

(defn create-request
  [token]
  (-> (mock/request :get "/api/sessions")
      (auth-header token)))

(deftest session-list-empty
  (facts
    "session-list-empty"
    (t/do-at
      std-time

      (db-setup prefix "setup")
      (let [expected-body (read-json prefix "empty-response")
            request (create-request "7b0e6756-d9e4-4001-9d53-000000000001")
            response (call-handler request)
            ]
        (verify-response response {:status :ok
                                   :body   expected-body})))))
