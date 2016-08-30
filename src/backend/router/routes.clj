(ns backend.router.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [backend.router.security :refer [anonymous authenticated roles own-user]]
            [backend.app.user.user-api :refer :all]
            [backend.app.session.session-api :refer :all]
            [backend.app.project.project-api :refer :all]
            ))

(defroutes ^:private user-routes
           (context "/users" []
             (POST "/" [] (anonymous user-api-create))
             (GET "/" [] (roles [:admin] user-api-list))
             (context "/:username" []
               (GET "/" [] (own-user user-api-read))
               (PUT "/" [] (own-user user-api-update))
               (DELETE "/" [] (own-user user-api-delete))
               (context "/actions" []
                 (PUT "/disable" [] (roles [:admin] user-api-disable))
                 (PUT "/activate" [] (roles [:admin] user-api-activate))
                 ))))

(defroutes ^:private session-routes
           (context "/sessions" []
             (POST "/" [] (anonymous session-api-create))
             (GET "/" [] (roles [:admin] session-api-list))
             (DELETE "/" [] (authenticated session-api-delete))
             ))

(defroutes ^:private project-routes
           (context "/projects" []
             (POST "/" [] (roles [:user] project-api-create))
             (GET "/" [] (roles [:user] project-api-list))
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
