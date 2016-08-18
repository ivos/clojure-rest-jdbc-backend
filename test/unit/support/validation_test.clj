(ns unit.support.validation-test
  (:use midje.sweet backend.support.validation)
  (:require [slingshot.slingshot :refer [try+]])
  )

(facts
  "validate"
  (fact
    "empty"
    (let [attributes {}
          data {}]
      (validate attributes data) => nil))
  (fact
    "required filled"
    (let [attributes {
                      :str1  {:required true :min-length 10 :max-length 10 :pattern #"[0-9]+"}
                      :enum1 {:required true :enum [:val1 :val2 :val3]}
                      :int1  {:required true}
                      }
          data {:str1 "1234567890" :enum1 "val2" :int1 123}]
      (validate attributes data) => nil))
  (fact
    "enum value types"
    (let [attributes {
                      :enumKeywords       {:enum [:val1 :val2 :val3]}
                      :enumStrings        {:enum ["val1" "val2" "val3"]}
                      :keywordsEnumString {:enum [:val1 :val2 :val3]}
                      }
          data {:enumKeywords :val2 :enumStrings "val2" :keywordsEnumString "val2"}]
      (validate attributes data) => nil))
  (fact
    "optional filled"
    (let [attributes {
                      :str1  {:min-length 10 :max-length 10 :pattern #"[0-9]+"}
                      :enum1 {:enum [:val1 :val2 :val3]}
                      :int1  {}
                      }
          data {:str1 "1234567890" :enum1 "val2" :int1 123}]
      (validate attributes data) => nil))
  (fact
    "optional empty"
    (let [attributes {
                      :str1  {:min-length 10 :max-length 10 :pattern #"[0-9]+"}
                      :enum1 {:enum [:val1 :val2 :val3]}
                      :int1  {}
                      }
          data {}]
      (validate attributes data) => nil))
  (fact
    "failure required"
    (let [attributes {
                      :str1      {:required true :min-length 10 :max-length 10 :pattern #"[0-9]+"}
                      :enum1     {:required true :enum [:val1 :val2 :val3]}
                      :blank-str {:required true}
                      :int1      {:required true}
                      }
          data {:blank-str " \t\n "}]
      (try+ (do
              (validate attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:enum1     [[:required]],
                         :str1      [[:required]],
                         :blank-str [[:required]],
                         :int1      [[:required]]}))))
  (fact
    "failure min-length"
    (let [attributes {:str1 {:min-length 10}}
          data {:str1 "123456789"}]
      (try+ (do
              (validate attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:str1 [[:min.length 10]]}))))
  (fact
    "failure max-length"
    (let [attributes {:str1 {:max-length 10}}
          data {:str1 "12345678901"}]
      (try+ (do
              (validate attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:str1 [[:max.length 10]]}))))
  (fact
    "failure enum"
    (let [attributes {:enum1 {:enum [:val1 :val2 :val3]}}
          data {:enum1 "valX"}]
      (try+ (do
              (validate attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:enum1 [[:enum [:val1 :val2 :val3]]]}))))
  (fact
    "failure pattern"
    (let [attributes {:str1 {:pattern #"[a-d]*"}}
          data {:str1 "abcde"}]
      (try+ (do
              (validate attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:str1 [[:pattern "[a-d]*"]]}))))
  (fact
    "failure multiple"
    (let [attributes {
                      :req1  {:required true}
                      :str1  {:min-length 11 :max-length 9 :pattern #"[1-9]+"}
                      :enum1 {:enum [:val1]}
                      }
          data {:str1 "1234567890" :enum1 "valX" :invalid "some"}]
      (try+ (do
              (validate attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:enum1   [[:enum [:val1]]],
                         :invalid [[:invalid.attribute]],
                         :req1    [[:required]],
                         :str1    [[:min.length 11] [:max.length 9] [:pattern "[1-9]+"]]}))))
  )
