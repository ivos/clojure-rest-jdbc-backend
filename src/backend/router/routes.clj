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
             (context "/:username" []
               (GET "/" [] user-api-read)
               (PUT "/" [] user-api-update)
               (DELETE "/" [] user-api-delete)
               (context "/actions" []
                 (PUT "/disable" [] user-api-disable)
                 (PUT "/activate" [] user-api-activate)
                 ))))

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
             (context "/:code" []
               (GET "/" [] project-api-read)
               (PUT "/" [] project-api-update)
               (DELETE "/" [] project-api-delete)
               )))

(defroutes app-handler
           (GET "/" [] "<h1>Backend</h1>")
           user-routes
           session-routes
           project-routes
           (route/not-found (fn [_] (resp/not-found {:code :route.not.found}))))
