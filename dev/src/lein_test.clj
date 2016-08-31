(ns lein-test
  (:gen-class)
  (:require [dev]
            [reloaded.repl :refer [system go stop]]
            [backend.system.flyway :refer [clean migrate]]
            [midje.repl :refer [load-facts]]
            ))

(defn run-test
  []
  (go)
  (clean (:flyway system))
  (migrate (:flyway system))
  (load-facts '*)
  (stop))
