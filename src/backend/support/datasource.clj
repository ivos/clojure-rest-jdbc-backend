(ns backend.support.datasource
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [hikari-cp.core :as ds]))

(defrecord DatasourceComponent [config datasource]
  component/Lifecycle
  (start [this]
    (if datasource
      this
      (let [db-config (get-in config [:config :db])]
        (log/info "Starting datasource.")
        (assoc this :datasource (ds/make-datasource db-config)))))
  (stop [this]
    (if (not datasource)
      this
      (do
        (log/info "Stopping datasource.")
        (try
          (ds/close-datasource datasource)
          (catch Throwable t
            (log/warn t "Error stopping datasource.")))
        (assoc this :datasource nil)))))

(defn new-datasource []
  (component/using
    (map->DatasourceComponent {})
    [:config]))
