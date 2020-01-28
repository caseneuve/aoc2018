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

(deftest input->str-test
  (testing "should return lazy seq of strings"
    (with-redefs [slurp (constantly "first\n second\nthird \n")]
      (is (= ["first" "second" "third"] (input->str "filename"))))))

(deftest input->ints-test
  (testing "should convert lines as strings to integers"
    (with-redefs [slurp (constantly "1\n2\n3\n4\n5\n")]
      (is (= [1 2 3 4 5] (input->ints "filename"))))))

(deftest input->xy-test
  (testing "should convert lines with coordinates as strings to lists of x, y integers"
    (with-redefs [slurp (constantly "1, 2\n2, 3\n3, 4\n4, 5\n")]
      (is (= '((1 2) (2 3) (3 4) (4 5)) (input->xy "filename"))))))




