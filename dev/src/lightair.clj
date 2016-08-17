(ns lightair
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component])
  ;(:use midje.sweet)
  (:import net.sf.lightair.Api))

(defrecord LightAirComponent [properties-file-name lightair]
  component/Lifecycle
  (start [this]
    (if lightair
      this
      (do
        (log/info "Starting LightAir.")
        (Api/initialize properties-file-name)
        (assoc this :lightair :initialized))))
  (stop [this]
    (if (not lightair)
      this
      (do
        (log/info "Stopping LightAir.")
        (Api/shutdown)
        (assoc this :lightair nil)))))

(defn new-lightair []
  (map->LightAirComponent {:properties-file-name "dev/resources/light-air.properties"}))


(defn- process-files
  [prefix files]
  (vec (map #(str "test/it/" prefix %1) files)))

(defn db-setup
  [prefix & files]
  (Api/setup {Api/DEFAULT_PROFILE, (process-files prefix files)}))

(defn db-verify
  [prefix & files]
  (Api/verify {Api/DEFAULT_PROFILE, (process-files prefix files)})
  ;((try
  ;    (catch AssertionError e
  ;      (fact "Database verification failed." (.getMessage e) => nil)
  ;      )))
  )
