(ns it.test-support
  (:require [clojure.edn :as edn]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [midje.sweet :refer [fact]]
            [backend.support.ring :refer :all]
            [reloaded.repl :refer [system]]
            ))

(def std-time (t/date-time 2015 10 11 12 34 56 123))

(defn get-config
  []
  (get-in system [:config :config]))

(defn get-handler
  []
  (get-in system [:handler :handler]))

(defn call-handler-at-std-time
  [request]
  (t/do-at std-time ((get-handler) request)))

(defn read-json
  [prefix path]
  (-> (str "test/it/" prefix path ".json") slurp json/decode json/encode))

(defn is-response-json
  [response]
  (fact "Response content type"
        (get-in response [:headers "Content-Type"]) => "application/json; charset=utf-8"))

(defn is-response-created
  [response expected-body]
  (let [location (get-in response [:headers "Location"] "")
        location-start (get-in (get-config) [:app :deploy-url])]
    (fact "Status code"
          (:status response) => (status-code :created))
    (is-response-json response)
    (fact "ETag"
          (get-in response [:headers "ETag"]) => "1")
    (fact "Response body"
          (:body response) => expected-body)
    (fact "Location start"
          (subs location 0 (min (count location-start) (count location))) => location-start)
    ))

(defn is-response-ok
  [response expected-body]
  (fact "Status code"
        (:status response) => (status-code :ok))
  (is-response-json response)
  (fact "Response body"
        (:body response) => expected-body)
  )

(defn is-response-ok-version
  [response expected-body version]
  (fact "Status code"
        (:status response) => (status-code :ok))
  (is-response-json response)
  (fact "ETag"
        (get-in response [:headers "ETag"]) => (str version))
  (fact "Response body"
        (:body response) => expected-body)
  )

(defn is-response-conflict
  [response version]
  (fact "Status code"
        (:status response) => (status-code :conflict))
  (fact "ETag"
        (get-in response [:headers "ETag"]) => (str version))
  )

(defn is-response-precondition-required
  [response]
  (fact "Status code"
        (:status response) => (status-code :precondition-required))
  )

(defn not-found-test
  [handler request]
  (let [response (handler request)
        expected-body (read-json "" "not-found-response")
        ]
    (fact "Status code"
          (:status response) => (status-code :not-found))
    (is-response-json response)
    (fact "Response body"
          (:body response) => expected-body)
    ))
