(ns backend.main
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [backend.system.config :refer :all]
            [backend.system.datasource :refer :all]
            [backend.system.runtime :refer [add-shutdown-hook]]
            [backend.system.flyway :refer :all]
            [backend.router.handler :refer :all]
            [backend.system.jetty :refer :all]))

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
      (add-shutdown-hook ::stop-system #(stop-system system))
      (migrate flyway))
    (log/info "Backend started OK.")))
