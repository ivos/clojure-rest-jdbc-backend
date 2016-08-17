(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [reloaded.repl :refer [system init start stop go reset]]
            [backend.main :refer [load-system-production]]
            [lightair :refer :all]
            [backend.support.flyway :refer :all]
            )
  (:use midje.repl))

(defn new-system []
  (let [system-production (load-system-production)
        system (assoc system-production
                 :lightair (new-lightair "dev/resources/light-air.properties"
                                         ))]
    system))

(reloaded.repl/set-init! new-system)

(defn db-clean
  []
  (clean (:flyway system)))

(defn db-migrate
  []
  (migrate (:flyway system)))

(defn db-recreate
  []
  (clean (:flyway system))
  (migrate (:flyway system)))
