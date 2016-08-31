(ns backend.router.security
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [clojure.set :as set]
            [clojure.data.codec.base64 :as b64]
            [slingshot.slingshot :refer [throw+]]
            [backend.app.session.session-logic :as session]
            [backend.support.ring :refer [status-code]]
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
      (let [session (session/session-logic-read-active ds token)]
        (if session
          (do
            (log/debug "Authenticated" session)
            session)
          (log/debug "Token not authenticated."))))
    (log/debug "Anonymous request.")))

(defn anonymous
  [handler]
  (fn
    [request]
    (log/debug "Anonymous request allowed.")
    (handler request)))

(defn authenticated
  [handler]
  (fn
    [request]
    (log/debug "Verifying user authenticated.")
    (when-not (:session request)
      (log/warn "User not authenticated.")
      (throw+ {:type     :custom-response
               :response {:status (status-code :unauthorized)}}))
    (handler request)))

(defn- get-current-user-roles
  [request]
  (if-let [roles-string (get-in request [:session :user :roles])]
    (->> (string/split roles-string #",")
         (map keyword)
         (set))
    #{}))

(defn roles
  [roles handler]
  (fn
    [request]
    (log/debug "Verifying user authorized.")
    (let [current-user-roles (get-current-user-roles request)]
      (when (empty? (set/intersection (set roles) current-user-roles))
        (log/warn "User not authorized, expected roles" roles "but has roles" current-user-roles)
        (throw+ {:type     :custom-response
                 :response {:status (status-code :forbidden)
                            :body   ["missing.role" roles current-user-roles]}})))
    (handler request)))

(defn own-user
  [handler]
  (fn
    [request]
    (log/debug "Verifying own user.")
    (let [current-username (get-in request [:session :user :username])
          request-username (get-in request [:params :username])]
      (when (not= current-username request-username)
        (log/warn "User not authorized, user" current-username "trying to operate on" request-username)
        (throw+ {:type     :custom-response
                 :response {:status (status-code :forbidden)
                            :body   ["not.own.user" request-username current-username]}})))
    (handler request)))
