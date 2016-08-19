(ns backend.support.validation
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+ try+]]
            [backend.support.ring :refer [status-code]]
            ))

(defn- validate-required
  [attr-name validation value]
  (when (and validation
             (or (nil? value)
                 (and (string? value)
                      (string/blank? value))))
    [attr-name :required]))

(defn- validate-min-length
  [attr-name validation value]
  (when (and (not (nil? value)) (< (count value) validation))
    [attr-name :min.length validation]))

(defn- validate-max-length
  [attr-name validation value]
  (when (> (count value) validation)
    [attr-name :max.length validation]))

(defn- validate-enum
  [attr-name validation value]
  (let [to-str #(if (nil? %1) "" (name %1))
        entity (set (map to-str validation))]
    (when (and (not (nil? value)) (not (contains? entity (to-str value))))
      [attr-name :enum validation])))

(defn- validate-pattern
  [attr-name validation value]
  (when (and (not (nil? value)) (not (re-matches validation value)))
    [attr-name :pattern (str validation)]))

(defn- validate-attribute-property
  [attr-name type validation value]
  (condp = type
    :required (validate-required attr-name validation value)
    :min-length (validate-min-length attr-name validation value)
    :max-length (validate-max-length attr-name validation value)
    :enum (validate-enum attr-name validation value)
    :pattern (validate-pattern attr-name validation value)
    :direction nil
    ))

(defn- validate-attribute
  [attribute value]
  (log/trace "Validating" (str \[ value \]) "as" attribute)
  (map
    #(validate-attribute-property (first attribute) (first %1) (second %1) value)
    (second attribute)))

(defn- verify-keys
  [inbound-attributes entity]
  (let [keys (set/difference (set (keys entity)) (set (keys inbound-attributes)))]
    (zipmap keys (repeat :invalid.attribute))))

(defn- group-errors
  [errors]
  (let [keys (distinct (map first errors))
        is-name #(= %1 (first %2))
        filter-by-name #(filter (partial is-name %1) errors)
        errors-grouped-by-name (map filter-by-name keys)
        removed-name (map #(map next %1) errors-grouped-by-name)
        entity (map vec (map #(map vec %1) removed-name))
        ]
    (zipmap keys entity)))

(defn- get-inbound
  [attributes]
  (let [f #(let [v (second %)]
            (or (nil? v)
                (nil? (:direction v))
                (not= :out (:direction v))))]
    (apply array-map (into [] cat (filter f attributes)))))

(defn- assoc-missing-keys
  [inbound-attributes entity]
  (let [missing-keys (set/difference (set (keys inbound-attributes)) (set (keys entity)))]
    (if (not-empty missing-keys)
      (apply assoc entity (interleave missing-keys (repeat nil)))
      entity)))

(defn valid
  "Validate entity."
  [attributes entity]
  (let [inbound-attributes (get-inbound attributes)
        attribute-errors (mapcat #(validate-attribute %1 (get entity (first %1))) inbound-attributes)
        invalid-key-errors (verify-keys inbound-attributes entity)
        errors (filter identity (concat attribute-errors invalid-key-errors))
        result (group-errors errors)]
    (when (not-empty errors)
      (log/debug "Validation failure" result)
      (throw+ {:type :validation-failure :errors result}))
    (assoc-missing-keys inbound-attributes entity)))
