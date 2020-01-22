(ns aoc18.puzzle03-test
  (:require
   [aoc18.puzzle03 :refer :all]
   [clojure.test :refer :all]))

(deftest parse-claim-test
  (testing "Should return all numbers found in given str ordered by keys in a hash map"
    (is (= {:id 1 :x 393 :y 863 :width 11 :height 29}
           (parse-claim "#1 @ 393,863: 11x29")))))

(deftest covered-by-test
  (testing "Should return a seq of vectors containing x and y positions of a rectangle 
            passed as an arg"
    (is (= '([1 1] [1 2] [2 1] [2 2])
           (covered-by {:id 1 :x 1 :y 1 :width 2 :height 2})))))

(deftest update-seen-replaces-test
  (testing "Should update old value adding new"
    (let [before {"a" #{1}}
          after (update before "a" update-seen {:id 2})]
      (is (= #{1 2} (get after "a"))))))

(deftest update-seen-creates-test
  (testing "Should create new value because there was none"
    (let [before {}
          after (update before "a" update-seen {:id 3})]
      (is (= #{3} (get after "a"))))))

(deftest loop-and-reduce-overlapping-test
  (testing "Should return hash map where keys are x, y positions and values are ids of 
            rectangles covering those positions"
    (let [rects [{:id "a" :x 1 :y 1 :width 2 :height 2}
                 {:id "b" :x 2 :y 2 :width 2 :height 2}]
          seen {}
          expected {[1 1] #{"a"}
                    [2 1] #{"a"}
                    [1 2] #{"a"}
                    [2 2] #{"a" "b"}
                    [3 2] #{"b"}
                    [2 3] #{"b"}
                    [3 3] #{"b"}}]
      (is (= expected (reduce loop-overlapping seen rects)))
      (is (= expected (reduce-overlapping rects))))))

(deftest example-test
  (testing "should return 4 for the first part and 3 for the second "
    (is (= {:part1 4 :part2 3}
           (solve "day03-ex")))))
