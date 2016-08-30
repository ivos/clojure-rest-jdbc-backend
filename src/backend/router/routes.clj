(ns backend.router.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [backend.router.security :refer [authenticated]]
            [backend.app.user.user-api :refer :all]
            [backend.app.session.session-api :refer :all]
            [backend.app.project.project-api :refer :all]
            ))

(defroutes ^:private user-routes
           (context "/users" []
             (POST "/" [] user-api-create)
             (GET "/" [] user-api-list)
             (GET "/:username" [] user-api-read)
             (PUT "/:username" [] user-api-update)
             (PUT "/:username/actions/disable" [] user-api-disable)
             (PUT "/:username/actions/activate" [] user-api-activate)
             (DELETE "/:username" [] user-api-delete)
             ))

(defroutes ^:private session-routes
           (context "/sessions" []
             (POST "/" [] session-api-create)
             (GET "/" [] session-api-list)
             (DELETE "/" [] (authenticated session-api-delete))
             ))

(defroutes ^:private project-routes
           (context "/projects" []
             (POST "/" [] (authenticated project-api-create))
             (GET "/" [] project-api-list)
             (GET "/:code" [] project-api-read)
             (PUT "/:code" [] project-api-update)
             (DELETE "/:code" [] project-api-delete)
             ))

(defroutes app-handler
           (GET "/" [] "<h1>Backend</h1>")
           user-routes
           session-routes
           project-routes
           (route/not-found (fn [_] (resp/not-found {:code :route.not.found}))))
