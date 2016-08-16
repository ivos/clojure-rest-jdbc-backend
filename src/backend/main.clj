(ns backend.main
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [backend.support.config :refer :all]
            [backend.support.datasource :refer :all]
            [backend.support.runtime :refer [add-shutdown-hook]]
            [backend.support.flyway :refer :all]
            [backend.router.handler :refer :all]
            [backend.support.jetty :refer :all]))

(defn load-system-production
  []
  (component/system-map
    :config (new-config "config.edn")
    :datasource (new-datasource)
    :flyway (new-flyway)
    :handler (new-handler)
    :jetty (new-jetty)
    ))

(defn -main [& args]
  (let [system (load-system-production)]
    (log/info "Backend is starting...")
    (let [system (component/start system)
          flyway (:flyway system)]
      (add-shutdown-hook ::stop-system #(component/stop system))
      (migrate flyway)
      )
    (log/info "Backend started OK.")))
