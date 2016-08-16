(ns backend.router.handler
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [backend.router.middleware :refer :all]
            [backend.router.routes :refer :all]
            ))

(defn- create-instance
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
