(ns backend.router.security
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [clojure.data.codec.base64 :as b64]
            [slingshot.slingshot :refer [throw+]]
            [backend.app.session.session-logic :refer :all]
            [backend.support.ring :refer :all]
            ))

(def ^:private auth-header-prefix
  "Basic ")

(defn- parse-auth-token
  [request]
  (when-let [authorization (get-in request [:headers "authorization"])]
    (when (string/starts-with? authorization auth-header-prefix)
      (let [basic-b64 (subs authorization (count auth-header-prefix))
            basic (String. (b64/decode (.getBytes basic-b64)))
            token (first (string/split basic #":"))]
        token))))

(defn get-session
  [{:keys [ds] :as request}]
  (if-let [token (parse-auth-token request)]
    (do
      (log/debug "Authenticating token" token)
      (let [session (session-logic-read-active ds token)]
        (if session
          (do
            (log/debug "Authenticated" session)
            session)
          (log/debug "Token not authenticated."))))
    (log/debug "Anonymous request.")))

(defn authenticated
  [handler]
  (fn
    [request]
    (when-not (:session request)
      (throw+ {:type     :custom-response
               :response {:status (status-code :unauthorized)
                          :body   {:code :no.valid.session}}}))
    (handler request)))
