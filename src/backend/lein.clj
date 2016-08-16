(ns backend.lein
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [backend.support.config :refer :all]
            [backend.support.datasource :refer :all]
            [backend.support.flyway :refer :all]))

(defn- load-system-flyway
  []
  (component/system-map
    :config (new-config "config.edn")
    :datasource (new-datasource)
    :flyway (new-flyway)
    ))

(defn db-clean
  []
  (let [system (component/start (load-system-flyway))
        flyway (:flyway system)]
    (try
      (clean flyway)
      (finally
        (component/stop system)))))

(defn db-migrate
  []
  (let [system (component/start (load-system-flyway))
        flyway (:flyway system)]
    (try
      (migrate flyway)
      (finally
        (component/stop system)))))

(defn db-recreate
  []
  (let [system (component/start (load-system-flyway))
        flyway (:flyway system)]
    (try
      (clean flyway)
      (migrate flyway)
      (finally
        (component/stop system)))))
