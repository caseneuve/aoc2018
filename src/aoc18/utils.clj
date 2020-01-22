(ns aoc18.utils
  (:require [clojure.string :as string]))


;;; * conversions
(defn str->int [str] (Integer/parseInt (string/trim str)))

;;; * read input
;; http://clojure-doc.org/articles/cookbooks/files_and_directories.html

(defn ensure-path
  "Returns str './inputs/FILENAME'"
  [filename]
  (let [dir "./inputs/"]
    (if (re-matches (re-pattern (str dir ".*")) filename)
      filename
      (str dir filename))))

(defn input->ints [filename]
  (map str->int
       (-> filename
           ensure-path
           slurp
           string/split-lines)))

(defn input->str [filename]
  (map string/trim
       (-> filename
           ensure-path
           slurp
           string/split-lines)))
