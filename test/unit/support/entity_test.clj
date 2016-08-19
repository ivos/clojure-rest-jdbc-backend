(ns unit.support.entity-test
  (:require [backend.support.entity])
  (:use midje.sweet backend.support.validation))

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
             :a 10
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
    (str (array-map
           :a 10
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
