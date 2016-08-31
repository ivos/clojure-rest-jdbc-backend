(ns backend.lein-ring
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [backend.system.config :refer [new-config]]
            [backend.system.datasource :refer [new-datasource]]
            [backend.system.flyway :refer [new-flyway migrate]]
            [backend.router.handler :refer [new-handler]]
            ))

(def ^:private system nil)

(def handler nil)

(defn- load-system-production
  []
  (component/system-map
    :config (new-config "config.edn")
    :datasource (new-datasource)
    :flyway (new-flyway)
    :handler (new-handler)
    ))

(defn init
  []
  (log/info "Backend is starting...")
  (alter-var-root #'system (constantly (load-system-production)))
  (alter-var-root #'system component/start)
  (migrate (:flyway system))
  (alter-var-root #'handler (constantly (get-in system [:handler :handler])))
  (log/info "Backend started OK."))

(defn destroy
  []
  (log/info "Backend is stopping...")
  (alter-var-root #'system component/stop)
  (alter-var-root #'handler (constantly nil))
  (log/info "Backend stopped OK."))
