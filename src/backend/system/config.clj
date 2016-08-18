(ns backend.system.config
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [com.stuartsierra.component :as component]))

(defn- parse-edn-resource
  [file-name]
  (-> file-name io/resource slurp edn/read-string))

(defrecord ConfigComponent [config-file-name config]
  component/Lifecycle
  (start [this]
    (if config
      this
      (do
        (log/info "Starting config.")
        (assoc this :config (parse-edn-resource config-file-name)))))
  (stop [this]
    (if (not config)
      this
      (do
        (log/info "Stopping config.")
        (assoc this :config nil)))))

(defn new-config [config-file-name]
  (map->ConfigComponent {:config-file-name config-file-name}))
