(ns backend.router.routes
  (:require [compojure.core :refer [defroutes context POST GET PUT DELETE]]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [backend.router.security :refer [anonymous authenticated roles own-user]]
            [backend.app.user.user-api :as user]
            [backend.app.session.session-api :as session]
            [backend.app.project.project-api :as project]
            ))

(defroutes ^:private user-routes
           (context "/users" []
             (POST "/" [] (anonymous user/user-api-create))
             (GET "/" [] (roles [:admin] user/user-api-list))
             (context "/:username" []
               (GET "/" [] (own-user user/user-api-read))
               (PUT "/" [] (own-user user/user-api-update))
               (DELETE "/" [] (own-user user/user-api-delete))
               (context "/actions" []
                 (PUT "/disable" [] (roles [:admin] user/user-api-disable))
                 (PUT "/activate" [] (roles [:admin] user/user-api-activate))
                 (POST "/switch-to" [] (roles [:admin] session/session-api-switch-to))
                 ))))

(defroutes ^:private session-routes
           (context "/sessions" []
             (POST "/" [] (anonymous session/session-api-create))
             (GET "/" [] (roles [:admin] session/session-api-list))
             (DELETE "/" [] (authenticated session/session-api-delete))
             ))

(defroutes ^:private project-routes
           (context "/projects" []
             (POST "/" [] (roles [:user] project/project-api-create))
             (GET "/" [] (roles [:user] project/project-api-list))
             (context "/:code" []
               (GET "/" [] (roles [:user] project/project-api-read))
               (PUT "/" [] (roles [:user] project/project-api-update))
               (DELETE "/" [] (roles [:user] project/project-api-delete))
               )))

(defroutes app-handler
           (GET "/" [] "<h1>Backend</h1>")
           user-routes
           session-routes
           project-routes
           (route/not-found (fn [_] (resp/not-found {:code :route.not.found}))))
