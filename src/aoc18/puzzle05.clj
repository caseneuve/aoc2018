(ns aoc18.puzzle05 (:require [aoc18.utils :refer [input->str]]
                             [clojure.repl :refer [doc]]))

(defn match? [a b] (= (java.lang.Math/abs (- a b)) 32))

(defn react [polymer]
  (count
   (reduce
    (fn [coll unit]
      (if (and (seq coll) (match? (peek coll) unit))
        (pop coll)
        (conj coll unit)))
    '() polymer)))

(defn remove-units [coll unit] (remove (hash-set unit (- unit 32)) coll))

(defn solve [input]
  (let [polymer (map int input)]
    {:part1 (time (react polymer))
     :part2 (time (apply min
                         (map #(react (remove-units polymer %))
                              (range 97 123))))
     }))
