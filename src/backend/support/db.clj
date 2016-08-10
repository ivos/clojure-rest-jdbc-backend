(ns backend.support.db
  (:require [clojure.tools.logging :as log]
            [hikari-cp.core :as ds]))

;(def datasource-config
;  (:db (read-string (slurp "resources/config.edn"))))

;(def datasource
;  (ds/make-datasource datasource-config))

;(def db-spec
;  {:datasource datasource})

(defn open-datasource!
  [config]
  (log/debug "Opening datasource.")
  (ds/make-datasource (:db config)))

(defn close-datasource!
  [datasource]
  (log/debug "Closing datasource.")
  (ds/close-datasource datasource))
