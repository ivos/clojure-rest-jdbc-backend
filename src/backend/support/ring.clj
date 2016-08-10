(ns backend.support.ring
  (:require [ring.util.response :refer :all]
            [slingshot.slingshot :refer [throw+]]
            ))

(def status-code
  {
   :ok                    200
   :created               201
   :no-content            204
   :unauthorized          401
   :not-found             404
   :method-not-allowed    405
   :conflict              409
   :unprocessable-entity  422
   :precondition-required 428
   })

(defn get-if-match
  [request]
  (let [version (get-in request [:headers "if-match"])]
    (if (nil? version)
      (throw+ {:type :custom-response :response {:status (status-code :precondition-required)}})
      version)))

(defn header-etag
  [response entity]
  (header response "ETag" (:version entity)))

(defn entity-result
  [entity]
  (dissoc entity :id :version))

(defn get-url
  [request & uri]
  (apply str (get-in request [:config :app :deploy-url]) uri))
