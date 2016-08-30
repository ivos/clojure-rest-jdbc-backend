(ns unit.support.repo-test
  (:require [backend.support.repo :refer :all]
            [midje.sweet :refer :all]
            [backend.support.validation :refer :all]
            ))

(facts
  "where-clause"
  (fact
    "empty"
    (#'backend.support.repo/where-clause {}) => [""])
  (fact
    "multiple"
    (#'backend.support.repo/where-clause {:a 1 :b "2" :c 3}) => ["a = ? and b = ? and c = ?" 1 "2" 3]))
