(ns aoc18.utils-test
  (:require
   [clojure.test :refer :all]
   [clojure.string :as string]
   [aoc18.utils :refer :all]))


(deftest ensure-path-add-dir-test
  (testing "should return filename preceded with 'inputs' dir"
    (is (= "./inputs/filename" (ensure-path "filename")))))

(deftest ensure-path-no-adding-dir-test
  (testing "should return filename preceded with 'inputs' dir"
    (is (= "./inputs/filename" (ensure-path "./inputs/filename")))))

(deftest str->int-test
  (testing "should trim and convert string to integer"
    (is (= [10 -10] (map str->int [" 10" " -10"])))))

(deftest read-input-raises-exception-test
  (testing "should raise FileNotFoundException"
    (is (thrown? java.lang.Exception (input->ints "nonexistent/file")))))

(deftest read-input-lines-test
  (testing "should return lazy seq of integers"
    (with-redefs [slurp (constantly "1\n-2\n10\n-99 \n")]
      (is (= (lazy-seq [1 -2 10 -99]) (input->ints "filename"))))))

(deftest input->str-test
  (testing "should return lazy seq of strings"
    (with-redefs [slurp (constantly "first\n second\nthird \n")]
      (is (= ["first" "second" "third"] (input->str "filename"))))))



