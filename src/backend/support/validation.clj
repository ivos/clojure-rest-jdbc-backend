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
        values (set (map to-str validation))]
    (when (and (not (nil? value)) (not (contains? values (to-str value))))
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
    ))

(defn- validate-attribute
  [attribute value]
  (log/trace "Validating " (str \[ value \]) "as" attribute)
  (map
    #(validate-attribute-property (first attribute) (first %1) (second %1) value)
    (second attribute)))

(defn- verify-keys
  [attributes values]
  (let [keys (set/difference (set (keys values)) (set (keys attributes)))]
    (zipmap keys (repeat :invalid.attribute))))

(defn- group-errors
  [errors]
  (let [keys (distinct (map first errors))
        is-name #(= %1 (first %2))
        filter-by-name #(filter (partial is-name %1) errors)
        errors-grouped-by-name (map filter-by-name keys)
        removed-name (map #(map next %1) errors-grouped-by-name)
        values (map vec (map #(map vec %1) removed-name))
        ]
    (zipmap keys values)))

(defn validate
  "Validate and convert values."
  [attributes values]
  (let [attribute-errors (mapcat #(validate-attribute %1 (get values (first %1))) attributes)
        invalid-key-errors (verify-keys attributes values)
        errors (filter identity (concat attribute-errors invalid-key-errors))
        result (group-errors errors)]
    (when (not-empty errors)
      (log/debug "Validation failure" result)
      (throw+ {:type :validation-failure :errors result}))))
