(ns unit.support.validation-test
  (:require [slingshot.slingshot :refer [try+]]
            [midje.sweet :refer :all]
            [backend.support.validation :refer :all]
            ))

(facts
  "validate"
  (fact
    "empty"
    (let [attributes {}
          data {}]
      (valid attributes data) => data))
  (fact
    "required filled"
    (let [attributes {
                      :str1  {:required true :min-length 10 :max-length 10 :pattern #"[0-9]+"}
                      :enum1 {:required true :enum [:val1 :val2 :val3]}
                      :int1  {:required true}
                      }
          data {:str1 "1234567890" :enum1 "val2" :int1 123}]
      (valid attributes data) => data))
  (fact
    "enum value types"
    (let [attributes {
                      :enumKeywords       {:enum [:val1 :val2 :val3]}
                      :enumStrings        {:enum ["val1" "val2" "val3"]}
                      :keywordsEnumString {:enum [:val1 :val2 :val3]}
                      }
          data {:enumKeywords :val2 :enumStrings "val2" :keywordsEnumString "val2"}]
      (valid attributes data) => data))
  (fact
    "optional filled"
    (let [attributes {
                      :str1  {:min-length 10 :max-length 10 :pattern #"[0-9]+"}
                      :enum1 {:enum [:val1 :val2 :val3]}
                      :int1  {}
                      }
          data {:str1 "1234567890" :enum1 "val2" :int1 123}]
      (valid attributes data) => data))
  (fact
    "optional empty"
    (let [attributes {
                      :str1  {:min-length 10 :max-length 10 :pattern #"[0-9]+"}
                      :enum1 {:enum [:val1 :val2 :val3]}
                      :int1  {}
                      }
          data {}]
      (valid attributes data) => {:enum1 nil, :int1 nil, :str1 nil}))
  (fact
    "outbound ok"
    (let [attributes {
                      :out1     {:required true :direction :out}
                      :in1      {:required true :direction :in}
                      :inOut1   {:required true :direction :in-out}
                      :default1 {:required true}
                      }
          data {:in1 1 :inOut1 2 :default1 3}]
      (valid attributes data) => data))
  (fact
    "outbound failure"
    (let [attributes {
                      :out1     {:required true :direction :out}
                      :in1      {:required true :direction :in}
                      :inOut1   {:required true :direction :in-out}
                      :default1 {:required true}
                      }
          data {}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:in1      [[:required]],
                         :inOut1   [[:required]],
                         :default1 [[:required]]}))))
  (fact
    "required failure"
    (let [attributes {
                      :str1      {:required true :min-length 10 :max-length 10 :pattern #"[0-9]+"}
                      :enum1     {:required true :enum [:val1 :val2 :val3]}
                      :blank-str {:required true}
                      :int1      {:required true}
                      }
          data {:blank-str " \t\n "}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:enum1     [[:required]],
                         :str1      [[:required]],
                         :blank-str [[:required]],
                         :int1      [[:required]]}))))
  (fact
    "min-length failure"
    (let [attributes {:str1 {:min-length 10}}
          data {:str1 "123456789"}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:str1 [[:min.length 10]]}))))
  (fact
    "min-length conversion"
    (let [attributes {:str1 {:min-length 10}}
          data {:str1 12345678901}]
      (valid attributes data) => data))
  (fact
    "max-length failure"
    (let [attributes {:str1 {:max-length 10}}
          data {:str1 "12345678901"}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:str1 [[:max.length 10]]}))))
  (fact
    "max-length conversion"
    (let [attributes {:str1 {:max-length 10}}
          data {:str1 123456789}]
      (valid attributes data) => data))
  (fact
    "enum failure"
    (let [attributes {:enum1 {:enum [:val1 :val2 :val3]}}
          data {:enum1 "valX"}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:enum1 [[:enum [:val1 :val2 :val3]]]}))))
  (fact
    "pattern failure"
    (let [attributes {:str1 {:pattern #"[a-d]*"}}
          data {:str1 "abcde"}]
      (try+ (do
              (valid attributes data)
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
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:enum1   [[:enum [:val1]]],
                         :invalid [[:invalid.attribute]],
                         :req1    [[:required]],
                         :str1    [[:min.length 11] [:max.length 9] [:pattern "[1-9]+"]]}))))
  (fact
    "date ok"
    (let [attributes {:date1 {:type :date}}
          data {:date1 "2016-02-28"}]
      (valid attributes data) => data))
  (fact
    "date failure"
    (let [attributes {:date1 {:type :date}
                      :date2 {:type :date}}
          data {:date1 "2016-02-30"
                :date2 "invalid"}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:date1 [[:type :date]] :date2 [[:type :date]]}))))
  (fact
    "time ok"
    (let [attributes {:time1 {:type :time}}
          data {:time1 "12:34:56"}]
      (valid attributes data) => data))
  (fact
    "time failure"
    (let [attributes {:time1 {:type :time}
                      :time2 {:type :time}}
          data {:time1 "12:34:60"
                :time2 "invalid"}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:time1 [[:type :time]] :time2 [[:type :time]]}))))
  (fact
    "timestamp ok"
    (let [attributes {:timestamp1 {:type :timestamp}}
          data {:timestamp1 "2016-02-28T12:34:56.123Z"}]
      (valid attributes data) => data))
  (fact
    "timestamp failure"
    (let [attributes {:timestamp1 {:type :timestamp}
                      :timestamp2 {:type :timestamp}
                      :timestamp3 {:type :timestamp}
                      :timestamp4 {:type :timestamp}
                      }
          data {:timestamp1 "2016-02-30T12:34:56.123Z"
                :timestamp2 "2016-02-28T12:34:60.123Z"
                :timestamp3 "2016-02-28T12:34:56.123"
                :timestamp4 "invalid"}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:timestamp1 [[:type :timestamp]]
                         :timestamp2 [[:type :timestamp]]
                         :timestamp3 [[:type :timestamp]]
                         :timestamp4 [[:type :timestamp]]
                         }))))
  (fact
    "integer ok"
    (let [attributes {:integer1 {:type :integer}}
          data {:integer1 1234567890123}]
      (valid attributes data) => data))
  (fact
    "integer failure"
    (let [attributes {:integer1 {:type :integer}
                      :integer2 {:type :integer}}
          data {:integer1 123.4
                :integer2 "invalid"}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:integer1 [[:type :integer]]
                         :integer2 [[:type :integer]]}))))
  (fact
    "number ok"
    (let [attributes {:number1 {:type :number}
                      :number2 {:type :number}
                      :number3 {:type :number}}
          data {:number1 123.4
                :number2 123
                :number3 12345678.90123}]
      (valid attributes data) => data))
  (fact
    "number failure"
    (let [attributes {:number1 {:type :number}
                      :number2 {:type :number}}
          data {:number1 "123.4"
                :number2 "invalid"}]
      (try+ (do
              (valid attributes data)
              (fact "Should throw" true => false))
            (catch [:type :validation-failure] {:keys [errors]}
              errors => {:number1 [[:type :number]]
                         :number2 [[:type :number]]}))))
  )
