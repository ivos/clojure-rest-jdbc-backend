(ns backend.router.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [backend.logic.user :refer :all]
    ;[backend.logic.session :refer :all]
            [backend.logic.project :refer :all]
            ))

(defroutes ^:private user-routes
           (context "/users" []
             (POST "/" request (user-create request))
             (GET "/" request (user-list request))
             (GET "/:username" request (user-read request))
             (PUT "/:username" request (user-update request))
             (PUT "/:username/actions/disable" request (user-disable request))
             (DELETE "/:username" request (user-delete request))
             ))

;(defroutes ^:private session-routes
;           (context "/sessions" []
;             (POST "/" request (session-create request))
;             (GET "/active" request (session-list-active request))
;             ))

(defroutes ^:private project-routes
           (context "/projects" []
             (POST "/" request (project-create request))
             (GET "/" request (project-list request))
             (GET "/:code" request (project-read request))
             (PUT "/:code" request (project-update request))
             (DELETE "/:code" request (project-delete request))
             ))

(defroutes app-handler
           (GET "/" [] "<h1>Backend</h1>")
           user-routes
           ;session-routes
           project-routes
           (route/not-found (fn [_] (resp/not-found {:code :route.not.found}))))
