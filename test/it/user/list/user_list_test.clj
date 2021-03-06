(ns it.user.list.user-list-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/list/")

(defn- create-request
  [params]
  (-> (mock/request :get "/api/users" params)
      (auth-header "7b0e6756-d9e4-4001-9d53-000000000001")))

(deftest user-list-empty
  (facts
    "user-list-empty"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "empty-response")
          request (create-request {})
          response (call-handler request)
          ]
      (verify-response response {:status :ok
                                 :body   expected-body})
      )))
