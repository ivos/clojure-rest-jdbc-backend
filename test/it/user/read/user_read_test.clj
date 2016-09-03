(ns it.user.read.user-read-test
  (:require [backend.support.ring :refer :all]
            [clojure.test :refer [deftest]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/read/")

(defn create-request
  [username]
  (-> (mock/request :get (str "/api/users/" username))
      (auth-header "7b0e6756-d9e4-4001-9d53-000000000001")))

(deftest user-read
  (facts
    "user-read"
    (db-setup prefix "setup")
    (let [expected-body (read-json prefix "response")
          request (create-request "username_2")
          response (call-handler request)
          ]
      (verify-response response {:status :ok
                                 :etag   12302
                                 :body   expected-body})
      )))
