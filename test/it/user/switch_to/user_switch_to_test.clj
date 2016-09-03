(ns it.user.switch-to.user-switch-to-test
  (:require [clojure.string :as string]
            [clojure.test :refer [deftest]]
            [backend.support.ring :refer :all]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [clj-time.core :as t]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "user/switch_to/")

(defn- create-request
  []
  (-> (mock/request :post "/api/users/username_to/actions/switch-to")
      (auth-header "7b0e6756-d9e4-4001-9d53-000000000001")))

(defn- ok
  [test-case]
  (t/do-at
    std-time

    (db-setup prefix "setup")
    (let [expected-body (read-json prefix (str test-case "-response"))
          request (create-request)
          response (call-handler request)
          response (update response :body string/replace #"\"token\":\"[0-9a-f\-]{36}\"" "\"token\":\"REPLACED\"")
          ]
      (verify-response response {:status :created
                                 :body   expected-body})
      (db-verify prefix (str test-case "-verify"))
      )))

(deftest user-switch-to-ok
  (facts
    "user-switch-to-ok"
    (ok "ok")))
