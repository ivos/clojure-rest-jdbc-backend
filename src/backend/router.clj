(ns backend.router
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [not-found header]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer :all]
            [ring.middleware.keyword-params :refer :all]
            [slingshot.slingshot :refer [try+]]
            [backend.support.util :refer [filter-password]]
            [backend.support.validation :refer [wrap-validation]]
            [backend.support.ring :refer :all]
            ;[backend.logic.user :refer :all]
            ;[backend.logic.session :refer :all]
            [backend.logic.project :refer :all]
            ))

;(defroutes ^:private user-routes
;           (context "/users" []
;             (POST "/" request (user-create request))
;             (GET "/" request (user-list request))
;             (GET "/:id" request (user-read request))
;             (PUT "/:id" request (user-update request))
;             (DELETE "/:id" request (user-delete request))
;             ))

;(defroutes ^:private session-routes
;           (context "/sessions" []
;             (POST "/" request (session-create request))
;             (GET "/active" request (session-list-active request))
;             ))

(defroutes ^:private project-routes
           (context "/projects" []
             (POST "/" request (project-create request))
             ;(GET "/" request (project-list request))
             ;(GET "/:id" request (project-read request))
             ;(PUT "/:id" request (project-update request))
             ;(DELETE "/:id" request (project-delete request))
             ))

(defroutes app-handler
           (GET "/" [] "<h1>Hello compojure</h1>")
           ;user-routes
           ;session-routes
           project-routes
           (route/not-found (fn [_] (not-found {:code :route.not.found}))))

(defn- wrap-config
  [handler config]
  (fn
    [request]
    (let [request-wrapped (assoc request :config config)]
      (handler request-wrapped))))

(defn- wrap-datasource
  [handler datasource]
  (fn
    [request]
    (let [request-wrapped (assoc request :ds {:datasource datasource})]
      (handler request-wrapped))))

(defn- wrap-conflict
  [handler]
  (fn
    [request]
    (try+ (handler request)
          (catch [:type :optimistic-locking-failure] {:keys [:v]}
            (header {:status (status-code :conflict)} "ETag" v)))))

(defn- wrap-unique-attribute-violation
  [handler]
  (fn
    [request]
    (try+ (handler request)
          (catch [:type :unique-attribute-constraint-violation] {:keys [:a :message]}
            (log/info message)
            {:status (status-code :unprocessable-entity)
             :body   {(name a) ["already.exists"]}}))))

(defn- wrap-custom-response
  [handler]
  (fn
    [request]
    (try+ (handler request)
          (catch [:type :custom-response] {:keys [:response]}
            response))))

(defn- wrap-log
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

(defn create-handler
  [config datasource]
  (-> app-handler
      wrap-validation
      wrap-conflict
      wrap-unique-attribute-violation
      wrap-custom-response
      wrap-log
      (wrap-json-body (:json config))
      wrap-json-response
      wrap-keyword-params
      wrap-params
      (wrap-datasource datasource)
      (wrap-config config)
      ))

(defn start-router!
  "Start HTTP server."
  [config datasource]
  (run-jetty (create-handler config datasource) (:jetty config)))
