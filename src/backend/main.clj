(ns backend.main
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [backend.system.config :refer [new-config]]
            [backend.system.datasource :refer [new-datasource]]
            [backend.system.runtime :as runtime]
            [backend.system.flyway :refer [new-flyway migrate]]
            [backend.router.handler :refer [new-handler]]
            [backend.system.jetty :refer [new-jetty]]))

(defn load-system-production
  []
  (component/system-map
    :config (new-config "config.edn")
    :datasource (new-datasource)
    :flyway (new-flyway)
    :handler (new-handler)
    :jetty (new-jetty)
    ))

(defn- stop-system
  [system]
  (log/info "Backend is stopping...")
  (component/stop system)
  (log/info "Backend stopped OK."))

(defn -main [& args]
  (let [system (load-system-production)]
    (log/info "Backend is starting...")
    (let [system (component/start system)
          flyway (:flyway system)]
      (runtime/add-shutdown-hook ::stop-system #(stop-system system))
      (migrate flyway))
    (log/info "Backend started OK.")))
