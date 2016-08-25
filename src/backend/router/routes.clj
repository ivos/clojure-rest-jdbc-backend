(ns backend.router.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [backend.app.user.user-api :refer :all]
            [backend.app.session.session-api :refer :all]
            [backend.app.project.project-api :refer :all]
            ))

(defroutes ^:private user-routes
           (context "/users" []
             (POST "/" request (user-api-create request))
             (GET "/" request (user-api-list request))
             (GET "/:username" request (user-api-read request))
             (PUT "/:username" request (user-api-update request))
             (PUT "/:username/actions/disable" request (user-api-disable request))
             (PUT "/:username/actions/activate" request (user-api-activate request))
             (DELETE "/:username" request (user-api-delete request))
             ))

(defroutes ^:private session-routes
           (context "/sessions" []
             (POST "/" request (session-api-create request))
             ;(GET "/active" request (session-list-active request))
             ))

(defroutes ^:private project-routes
           (context "/projects" []
             (POST "/" request (project-api-create request))
             (GET "/" request (project-api-list request))
             (GET "/:code" request (project-api-read request))
             (PUT "/:code" request (project-api-update request))
             (DELETE "/:code" request (project-api-delete request))
             ))

(defroutes app-handler
           (GET "/" [] "<h1>Backend</h1>")
           user-routes
           session-routes
           project-routes
           (route/not-found (fn [_] (resp/not-found {:code :route.not.found}))))
