(ns backend.router.handler
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [ring.middleware.json :as json]
            [ring.middleware.params :as params]
            [ring.middleware.keyword-params :as keyword-params]
            [backend.router.middleware :as mware]
            [backend.router.routes :as routes]
            ))

(defn- create-instance
  [config datasource]
  (-> routes/app-handler
      (mware/wrap-authentication)
      (mware/wrap-validation)
      (mware/wrap-custom-response)
      (mware/wrap-log)
      (json/wrap-json-body (:json config))
      (json/wrap-json-response)
      (keyword-params/wrap-keyword-params)
      (params/wrap-params)
      (mware/wrap-datasource datasource)
      (mware/wrap-config config)
      ))

(defrecord HandlerComponent [config datasource handler]
  component/Lifecycle
  (start [this]
    (if handler
      this
      (do
        (log/info "Starting handler.")
        (assoc this :handler (create-instance (:config config) (:datasource datasource))))))
  (stop [this]
    (if (not handler)
      this
      (do
        (log/info "Stopping handler.")
        (assoc this :handler nil)))))

(defn new-handler []
  (component/using
    (map->HandlerComponent {})
    [:config :datasource]))
