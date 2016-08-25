(ns it.test-support
  (:require [clojure.string :as string]
            [ring.mock.request :as mock]
            [clj-time.core :as t]
            [midje.sweet :refer [fact]]
            [backend.support.ring :refer :all]
            [reloaded.repl :refer [system]]
            ))

(def std-time (t/date-time 2015 10 11 12 34 56 123))

(defn get-handler
  []
  (-> system :handler :handler))

(defn call-handler
  [request]
  ((get-handler) request))

(defn if-match-header
  [request version]
  (if (nil? version)
    request
    (mock/header request "If-Match" version)))

(defn read-json
  [prefix path]
  (-> (str "test/it/" prefix path ".json")
      slurp
      ; Cheshire does NOT keep the order of the fields in object maps!
      ; remove whitespace at the start of lines:
      (string/replace #"\n[\t]*" "")
      ; remove space after field name colon separator:
      (string/replace "\": " "\":")
      ))

(defn verify-response
  [response validations]
  (let [status (:status validations)]
    (fact "Status code"
          (:status response) => (status-code status)))
  (if-let [etag (:etag validations)]
    (fact "ETag"
          (get-in response [:headers "ETag"]) => (str etag))
    (fact "No ETag"
          (get-in response [:headers "ETag"]) => nil))
  (if-let [location (:location validations)]
    (fact "Location"
          (get-in response [:headers "Location"]) => location)
    (fact "No Location"
          (get-in response [:headers "Location"]) => nil))
  (if-let [body (:body validations)]
    (do
      (fact "Response content type"
            (get-in response [:headers "Content-Type"]) => "application/json; charset=utf-8")
      (fact "Response body"
            (:body response) => body))
    (fact "No response body"
          (:body response) => nil))
  )

(defn not-found-test
  [request]
  (let [response ((get-handler) request)
        expected-body (read-json "" "not-found-response")]
    (verify-response response {:status :not-found
                               :body   expected-body})
    ))
