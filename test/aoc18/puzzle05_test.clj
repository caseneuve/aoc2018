(ns aoc18.puzzle05-test
  (:require [aoc18.puzzle05 :refer :all]
            [clojure.test :refer :all]))

(deftest match-test
  (testing "the same letters compared should return true, otherwise false"
    (is (= true  (reduce match? (map int [\A \a]))))
    (is (= true  (reduce match? (map int [\b \B]))))
    (is (= false (reduce match? (map int [\c \D]))))
    (is (= false (reduce match? (map int [\f \g]))))
    (is (= false (reduce match? (map int [\H \I]))))))

(deftest remove-units-test
  (testing "should remove ints corresponding to lower- and uppercase letters from the
            collection"
    (is (= [\o \l \a \n]
           (vec (map char (remove-units (map int "Golang") (int \g))))))
    (is (= [\C \l \o \j \u \r \e]
           (vec (map char (remove-units (map int "Clojure") (int \x))))))))

(deftest solve-test
  (testing "should match examples"
    (is (= {:part1 10 :part2 4} (solve "dabAcCaCBAcCcaDA")))))
