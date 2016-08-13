(ns it.project.create.project-create-test
  (:use midje.sweet)
  (:require [ring.mock.request :as mock]
            [clj-time.core :as t]
            [backend.app :refer [config repl-handler]]
            [lightair :refer :all]
            [it.test-support :refer :all]
            ))

(def ^:private prefix "project/create/")

(defn- create-request
  [body]
  (-> (mock/request :post "/projects" body)
      (mock/content-type "application/json")))

(facts
  "Project create"
  (facts
    "Full"
    (db-setup prefix "setup.xml")
    (let [request-body (read-json prefix "full-request")
          expected-body (read-json prefix "full-response")
          request (create-request request-body)
          response (t/do-at std-time (repl-handler request))
          location (get-in response [:headers "Location"] "")
          id (-> location (.split "/") last)
          ]
      (is-response-created response expected-body config)
      (fact "Id"
            id => "code-1")
      (db-verify prefix "full-verify.xml")
      ))
  '(facts
     "Empty"
     (let [request-body "{}"
           response-body (read-json "project/create/empty-response")
           request (create-request request-body)
           response (repl-handler request)
           ]
       (fact "Status code"
             (:status response) => (status-code :unprocessable-entity))
       (is-response-json response)
       (fact "Response body"
             (:body response) => response-body)
       ))
  )
