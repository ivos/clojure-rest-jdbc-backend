(ns backend.router.routes
  (:require [compojure.core :refer [defroutes context POST GET PUT DELETE]]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [backend.router.security :refer [anonymous authenticated roles own-user own-user-or-roles]]
            [backend.app.user.user-api :as user]
            [backend.app.session.session-api :as session]
            [backend.app.project.project-api :as project]
            ))

(defroutes ^:private user-routes
           (context "/users" []
             (POST "/" [] (anonymous user/create))
             (GET "/" [] (roles [:admin] user/list))
             (context "/:username" []
               (GET "/" [] (own-user-or-roles [:admin] user/read))
               (PUT "/" [] (own-user user/update))
               (DELETE "/" [] (own-user user/delete))
               (context "/actions" []
                 (PUT "/disable" [] (roles [:admin] user/disable))
                 (PUT "/activate" [] (roles [:admin] user/activate))
                 (POST "/switch-to" [] (roles [:admin] session/switch-to))
                 ))))

(defroutes ^:private session-routes
           (context "/sessions" []
             (POST "/" [] (anonymous session/create))
             (GET "/" [] (roles [:admin] session/list))
             (DELETE "/" [] (authenticated session/delete))
             ))

(defroutes ^:private project-routes
           (context "/projects" []
             (POST "/" [] (roles [:user] project/create))
             (GET "/" [] (roles [:user] project/list))
             (context "/:code" []
               (GET "/" [] (roles [:user] project/read))
               (PUT "/" [] (roles [:user] project/update))
               (DELETE "/" [] (roles [:user] project/delete))
               )))

(defroutes app-handler
           (GET "/" [] "<h1>Backend</h1>")
           (context "/api" []
             user-routes
             session-routes
             project-routes
             )
           (route/not-found (fn [_] (resp/not-found {:code :route.not.found}))))
