(ns unit.support.entity-test
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [flatland.ordered.map :refer [ordered-map]]
            [backend.support.entity])
  (:use midje.sweet backend.support.validation)
  (:import (java.sql Time)))

(facts
  "conform-keys"
  (fact
    "empty"
    (#'backend.support.entity/conform-keys {} {}) => {})
  (fact
    "reorder keys"
    (str (#'backend.support.entity/conform-keys
           {:d 1
            :c 2
            :b 3
            :a 4}
           {:a nil
            :b nil
            :c nil}
           )) =>
    (str {
          :a 4
          :b 3
          :c 2
          })
    )
  (fact
    ; 9 keys (or more) switch automatically from array map to hash map
    ; and the order of keys is not retained anymore
    "10 keys"
    (str (#'backend.support.entity/conform-keys
           (array-map
             :j 1
             :i 2
             :h 3
             :g 4
             :f 5
             :e 6
             :d 7
             :c 8
             :b 9
             :a "10"
             )
           (array-map
             :a nil
             :b nil
             :c nil
             :d nil
             :e nil
             :f nil
             :g nil
             :h nil
             :i nil
             :j nil
             )
           )) =>
    (str (ordered-map
           :a "10"
           :b 9
           :c 8
           :d 7
           :e 6
           :f 5
           :g 4
           :h 3
           :i 2
           :j 1
           ))
    )
  )

(facts
  "format-data-types"
  (fact
    "empty"
    (#'backend.support.entity/format-data-types {} {}) => {})
  (fact
    "data types"
    (str (#'backend.support.entity/format-data-types
           {:date1      (tc/to-sql-date (t/local-date 2015 12 31))
            :time1      (-> (t/local-time 12 34 56)
                            .toDateTimeToday
                            (.withDate 1970 1 1)
                            (.withMillisOfSecond 0)
                            .getMillis
                            Time.)
            :timestamp1 (tc/to-sql-time (t/date-time 2015 12 31 12 34 56 123))
            :string1    "v1"}
           {:date1      {:type :date}
            :time1      {:type :time}
            :timestamp1 {}
            :string1    {}}
           )) =>
    (str (ordered-map
           :date1      "2015-12-31"
           :time1      "12:34:56"
           :timestamp1 (tc/to-sql-time (t/date-time 2015 12 31 12 34 56 123))
           :string1    "v1"))
    )
  )
