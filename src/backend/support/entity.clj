(ns backend.support.entity)

(defn entity-result
  [entity]
  (dissoc entity :id :version))

(defn list-entity-result
  [get-detail-uri request entity]
  (-> entity
      (assoc :uri (get-detail-uri request entity))
      (dissoc :id :version)))
