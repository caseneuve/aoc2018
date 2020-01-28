(ns aoc18.puzzle06-test (:require [aoc18.puzzle06 :refer :all]
                                  [aoc18.utils :refer [input->xy]]
                                  [clojure.test :refer :all]))

(deftest manhattan-test
  (testing "should return sum of absolute values of diffs of xs and ys"
    (is (= 2 (manhattan [1 0] [0 1])))))

  (deftest closest-test
    (testing "should match exemplary data"
      (let [coords (input->xy "day06-ex")]
        (is (= [1 1] (closest coords [4 0])))
        (is (= nil   (closest coords [5 0])))
        (is (= [3 4] (closest coords [4 2])))
        (is (= [5 5] (closest coords [5 2]))))))

