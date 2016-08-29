(ns backend.router.middleware
  (:require [clojure.tools.logging :as log]
            [ring.util.response :refer [header]]
            [slingshot.slingshot :refer [try+]]
            [backend.support.ring :refer :all]
            [backend.router.security :as security]
            [backend.support.util :refer [filter-password]]
            ))

(defn wrap-config
  [handler config]
  (fn
    [request]
    (let [request-wrapped (assoc request :config config)]
      (handler request-wrapped))))

(defn wrap-datasource
  [handler datasource]
  (fn
    [request]
    (let [request-wrapped (assoc request :ds {:datasource datasource})]
      (handler request-wrapped))))

(defn wrap-validation
  "Ring middleware to catch :validation-failure and convert it to HTTP 422 Unprocessable Entity response."
  [handler]
  (fn
    [request]
    (try+ (handler request)
          (catch [:type :validation-failure] {:keys [errors]}
            (let [response {:status (status-code :unprocessable-entity)
                            :body   errors}]
              (log/info "Validation failure" response)
              response)))))

(defn wrap-custom-response
  [handler]
  (fn
    [request]
    (try+ (handler request)
          (catch [:type :custom-response] {:keys [:response]}
            response))))

(defn wrap-log
  [handler]
  (fn
    [request]
    (let [request-info
          (clojure.string/join
            " "
            [(:protocol request)
             (-> request :request-method name clojure.string/upper-case)
             (:uri request)])
          body (when-not (#{:get :head :delete} (:request-method request))
                 (filter-password (:body request)))]
      (log/info ">>> Request"
                request-info
                "Params:" (:params request)
                "Body:" body
                "Headers:" (-> request :headers (dissoc "host" "content-length")))
      (let [response (handler request)]
        (log/info "<<< Response" request-info response)
        response))))

(defn wrap-authentication
  [handler]
  (fn
    [request]
    (let [session (security/get-session request)
          request-wrapped (assoc request :session session)]
      (handler request-wrapped))))
