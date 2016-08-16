(ns backend.support.flyway
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component])
  (import [org.flywaydb.core Flyway]))

(defprotocol FlywayProtocol
  (clean [this])
  (migrate [this])
  )

(defn- create-instance
  [datasource]
  (doto (Flyway.)
    (.setDataSource datasource)
    (.setSqlMigrationPrefix "")))

(defrecord FlywayComponent [datasource flyway]
  component/Lifecycle
  (start [this]
    (if flyway
      this
      (do
        (log/info "Starting Flyway.")
        (assoc this :flyway (create-instance (:datasource datasource))))))
  (stop [this]
    (if (not flyway)
      this
      (do
        (log/info "Stopping Flyway.")
        (assoc this :flyway nil))))
  FlywayProtocol
  (clean [this]
    (.clean (:flyway this)))
  (migrate [this]
    (.migrate (:flyway this)))
  )

(defn new-flyway []
  (component/using
    (map->FlywayComponent {})
    [:datasource]))
