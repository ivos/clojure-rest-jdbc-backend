(ns backend.support.migration
  (import [org.flywaydb.core Flyway]))

(defn- create-flyway
  [datasource]
  (doto (Flyway.)
    (.setDataSource datasource)
  	(.setSqlMigrationPrefix "")))

(defn migrate!
  [datasource]
  (.migrate (create-flyway datasource)))

(defn clean!
  [datasource]
  (.clean (create-flyway datasource)))
