(ns backend.support.datasource
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component])
  (:import [com.zaxxer.hikari HikariConfig HikariDataSource]))

(defn- make-config
  [{:keys [url username password conn-timeout idle-timeout
           max-lifetime conn-test-query min-idle max-pool-size pool-name]}]
  (let [cfg (HikariConfig.)]
    (when url (.setJdbcUrl cfg url))
    (when username (.setUsername cfg username))
    (when password (.setPassword cfg password))
    (when max-pool-size (.setMaximumPoolSize cfg max-pool-size))
    (when min-idle (.setMinimumIdle cfg min-idle))
    (when conn-timeout (.setConnectionTimeout cfg conn-timeout))
    (when idle-timeout (.setIdleTimeout cfg conn-timeout))
    (when max-lifetime (.setMaxLifetime cfg max-lifetime))
    (when conn-test-query (.setConnectionTestQuery cfg conn-test-query))
    (when pool-name (.setPoolName cfg pool-name))
    cfg))

(defrecord DatasourceComponent [config datasource]
  component/Lifecycle
  (start [this]
    (if datasource
      this
      (let [db-config (get-in config [:config :db])]
        (log/info "Starting datasource.")
        (assoc this :datasource (HikariDataSource. (make-config db-config)))
        )))
  (stop [this]
    (if (not datasource)
      this
      (do
        (log/info "Stopping datasource.")
        (try
          (.close datasource)
          (catch Throwable t
            (log/warn t "Error stopping datasource.")))
        (assoc this :datasource nil)))))

(defn new-datasource []
  (component/using
    (map->DatasourceComponent {})
    [:config]))
