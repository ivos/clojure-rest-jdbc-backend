(ns backend.support.util)

(defn filter-password
  [entity]
  (if (:password entity)
    (assoc entity :password "*****")
    entity))
