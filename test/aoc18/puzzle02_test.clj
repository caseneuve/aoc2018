(ns aoc18.puzzle02-test
  (:require [aoc18.utils :refer [input->str]]
            [aoc18.puzzle02 :refer :all]
            [clojure.test :refer :all]))

(deftest letter-repetition-test
  (testing "should return set of letter frequencies"
    (is (= #{1} (letter-repetition "abcdef")))
    (is (= #{1 2} (letter-repetition "abcabe")))
    (is (= #{1 2 3} (letter-repetition "abcabea")))))

(deftest two-threes-test
  (testing "should return vecor of ones and zeros, where indx 0 indicates if there are
            doubled letters, and idx 1 -- tripled letters"
    (is (= [0 0] (two-threes #{1})))
    (is (= [0 1] (two-threes #{1 3})))
    (is (= [1 0] (two-threes #{2 4})))
    (is (= [1 1] (two-threes #{1 2 3 4})))))

(deftest checksum-test
  (testing "should match puzzle 1 example"
    (is (= 12 (checksum (input->str "day02-ex1"))))))

(deftest compare-letters-test
  (testing "should return hashmap where keys are booleans and values are number of common
            letters, e.g. {true 3, false 1} means that there are three letters doubled 
            and one is not common"
    (is (= {false 4} (compare-letters "asdf" "qwer")))
    (is (= {true 3 false 1} (compare-letters "asdf" "asdq")))
    (is (= {true 2 false 2} (compare-letters "asdf" "askl")))))

(deftest find-similar-test
  (testing "should return list of two words that differ only by one letter"
    (is (= '("fghij" "fguij") (find-similar (input->str "day02-ex2"))))))

(deftest example2-test
  (testing "should match puzzle2 example"
    (let [words (find-similar (input->str "day02-ex2"))]
      (is (= "fgij" (time (common-str words))))
      (is (= "fgij" (time (common-str2 words)))))))

;; the test above is tricky because all letters are already sorted alphabetically
;; we need to test words which have random order of letters

(deftest common-str-test
  (testing "should return the same string"
    (let [words '("waxyhi" "wexyhi")]
      (is (= "wxyhi" (time (common-str words))))
      (is (= "wxyhi" (time (common-str2 words)))))))
