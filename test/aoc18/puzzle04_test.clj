(ns aoc18.puzzle04-test
  (:require [aoc18.puzzle04 :refer :all]
            [aoc18.utils :refer [input->str]]
            [clojure.test :refer :all]))

(deftest parse-test
  (testing "parse data and return hash-map with minutes and id if present"
    (is (= '(57 73) (parse "[1518-03-10 23:57] Guard #73 begins shift")))
    (is (= '(22 nil) (parse "[1518-03-11 00:22] wakes up")))))

(deftest check-in-test
  (testing "should return hash-map with ids and minutes"
    (is (= {10 [5 25 30 55 24 29], 99 [40 50 36 46 45 55]}
           (check-in (input->str "day04-ex"))))))

(deftest get-ranges-test
  (testing "should split vector of ints into a vector of lists - pairs"
    (is (= ['(1 2) '(3 4) '(5 6)] (get-ranges [1 2 3 4 5 6])))))

(deftest count-sleepy-test
  (testing "should return hash-map of minutes and their occurences"
    (is (= {1 1, 2 2, 3 2, 4 1, 5 1}
           (count-sleepy [1 4 4 6 2 4 ])))))

(deftest process-guard-data-test
  (testing "should return hash-map with keys :minute :occurence and :sleep-time"
    (is (= '({:minute 24, :occurence 2, :sleep-time 50}
             {:minute 45, :occurence 3, :sleep-time 30})
           (map process-guard-data
                [[5 25 30 55 24 29]
                 [40 50 36 46 45 55]])))))

(deftest process-logs-test
  (testing "should meet puzzle 4 example data"
    (is (= [{:id 10, :minute 24, :occurence 2, :sleep-time 50}
            {:id 99, :minute 45, :occurence 3, :sleep-time 30}]
           (process-logs (check-in (input->str "day04-ex")))))))

(deftest example-test
  (testing "should return 240 for the first part, and 4455 fot the second"
    (is (= {:part1 240 :part2 4455} (solve "day04-ex")))))
