(ns backend.router.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as resp]
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
           (GET "/" [] "<h1>Backend</h1>")
           ;user-routes
           ;session-routes
           project-routes
           (route/not-found (fn [_] (resp/not-found {:code :route.not.found}))))
