(ns aoc18.puzzle01 (:require [aoc18.utils :refer [input->ints]]
                             [clojure.repl :refer [doc]]))

(defn find-repeated-freq [input]
  (loop [[freq & rest] (reductions + (cycle input))
         seen #{0}]
    (if (contains? seen freq)   ; we might write (seen freq) as well
      freq
      (recur rest (conj seen freq)))))

(defn solve []
  (let [input (input->ints "day01")]
    {:part1 (time (apply + input))
     :part2 (time (find-repeated-freq input))}))
