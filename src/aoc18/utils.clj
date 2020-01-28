(ns aoc18.utils)

;;;* conversions
(defn str->int [str] (Integer/parseInt (clojure.string/trim str)))

;;;* read input
;; http://clojure-doc.org/articles/cookbooks/files_and_directories.html

(defn ensure-path
  "Returns str './inputs/FILENAME'"
  [filename]
  (let [dir "./inputs/"]
    (if (re-matches (re-pattern (str dir ".*")) filename)
      filename
      (str dir filename))))

;;;* to str
(defn input->str [filename]
  (map clojure.string/trim
       (-> filename
           ensure-path
           slurp
           clojure.string/split-lines)))

;;;* to ints
(defn input->ints [filename]
  (->> filename
       input->str
       (map str->int)))

;;;* to coordinates
(defn input->xy [filename]
  (->> filename
       input->str
       (map #(re-seq #"\d+" %))
       (map #(map str->int %))))
