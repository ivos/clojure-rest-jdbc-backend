(ns backend.support.ring
  (:require [ring.util.response :refer :all]
            [slingshot.slingshot :refer [throw+]]))

(def status-code
  {
   :ok                    200
   :created               201
   :no-content            204
   :unauthorized          401
   :forbidden             403
   :not-found             404
   :method-not-allowed    405
   :conflict              409
   :precondition-failed   412
   :unprocessable-entity  422
   :precondition-required 428
   })

(defn get-version
  [request]
  (let [version (get-in request [:headers "if-match"])]
    (cond
      (nil? version) (throw+
                       {:type     :custom-response
                        :response {:status (status-code :precondition-required)}})
      (not (integer? (read-string version))) (throw+
                                               {:type     :custom-response
                                                :response {:status (status-code :precondition-failed)}})
      :else (read-string version))))

(defn etag-header
  [response entity]
  (header response "ETag" (:version entity)))

(defn location-header
  [response location]
  (header response "Location" location))

(def response-no-content
  {:status  (status-code :no-content)
   :headers {}
   :body    nil})

(defn get-deploy-url
  [config & uri]
  (apply str (-> config :app :deploy-url) uri))
