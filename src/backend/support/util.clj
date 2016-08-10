(ns backend.support.util)

(defn filter-password
  [data]
  (if (:password data)
    (assoc data :password "*****")
    data))
