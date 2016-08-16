(ns lein-test
  (:gen-class)
  (:require [dev]
            [reloaded.repl :refer [system go stop]]
            [backend.support.flyway :refer :all]
            [midje.repl :refer [load-facts]]
            ))

(defn run-test
  []
  (go)
  (migrate (:flyway system))
  (load-facts '*)
  (stop))
