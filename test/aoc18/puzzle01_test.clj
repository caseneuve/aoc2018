(ns aoc18.puzzle01-test
  (:require [clojure.test :refer :all]
            [aoc18.puzzle01 :refer [find-repeated-freq]]))

(deftest find-repeated-freq-test
  (testing "should match exemplary data"
    (is (= 0  (find-repeated-freq [1 -1])))
    (is (= 10 (find-repeated-freq [3 3 4 -2 -4])))
    (is (= 5  (find-repeated-freq [-6 3 8 5 -6])))
    (is (= 14 (find-repeated-freq [7 7 -2 -7 -4])))))

