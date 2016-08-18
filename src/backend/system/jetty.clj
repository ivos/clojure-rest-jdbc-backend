(ns backend.system.jetty
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty])
  (:import org.eclipse.jetty.server.Server))

(defrecord JettyComponent [config handler jetty]
  component/Lifecycle
  (start [this]
    (if jetty
      this
      (let [options (assoc (:jetty (:config config)) :join? false)]
        (log/info "Starting Jetty.")
        (assoc this :jetty (jetty/run-jetty (:handler handler) options)))))
  (stop [this]
    (if (not jetty)
      this
      (do
        (log/info "Stopping Jetty.")
        (let [^Server server (:jetty this)]
          (try (.stop server)
               (.join server)
               (catch Throwable t
                 (log/warn t "Error stopping Jetty."))))
        (assoc this :jetty nil)))))

(defn new-jetty []
  (component/using
    (map->JettyComponent {})
    [:config :handler]))
