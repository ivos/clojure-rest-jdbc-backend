(ns backend.support.db
  (:require [clojure.tools.logging :as log]
            [hikari-cp.core :as ds]))

(defn open-datasource!
  [config]
  (log/debug "Opening datasource.")
  (ds/make-datasource (:db config)))

(defn close-datasource!
  [datasource]
  (log/debug "Closing datasource.")
  (ds/close-datasource datasource))
