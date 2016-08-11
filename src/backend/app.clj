(ns backend.app
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [backend.support.config :refer :all]
            [backend.support.db :refer :all]
            [backend.support.migration :refer :all]
            [backend.router :refer :all]
            ))

(def config (read-config))
(def ^:private datasource (open-datasource! config))

(defn start
  []
  (log/debug "Backend is starting.")
  (migrate! datasource))

(defn stop
  []
  (log/debug "Backend is stopping.")
  (close-datasource! datasource))

(defn db-clean
  []
  (try
    (clean! datasource)
    (finally
      (close-datasource! datasource))))

(defn db-migrate
  []
  (try
    (migrate! datasource)
    (finally
      (close-datasource! datasource))))

(defn db-recreate
  []
  (try
    (clean! datasource)
    (migrate! datasource)
    (finally
      (close-datasource! datasource))))

(defn -main
  [& _]
  (try
    (start)
    (start-router! config datasource)
    (finally
      (stop))))

(def repl-handler (create-handler config datasource))
