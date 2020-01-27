(ns aoc18.puzzle07 (:require [aoc18.utils :refer [input->str]]
                             [clojure.repl :refer [doc]]))

(defn update-step [old new] (if old (conj old new) #{new}))

(defn parse [input]
  (reduce (fn [[before after] [prec succ]] [(update before prec update-step succ)
                                           (update after succ update-step prec)])
          [{} {}] input))

(defn diff [a b]
  (->> [a b]
       (map set)
       (reduce clojure.set/difference)
       sort))

(defn match [steps candidates after]
  (filter
   #(when (empty? (clojure.set/difference (after %) (set steps))) %)
   candidates))

(defn walk [before after]
  (let [[ka kb] (map keys [after before])]
    (loop [steps [(first (diff kb ka))]]  ;; diff -> first step
      (if (= (count steps) (count kb))
        (conj steps (first (diff ka kb))) ;; reversed diff -> last step
        (let [candidates (match steps (diff kb steps) after)
              succ (first (sort (match steps candidates after)))]
          (recur (conj steps succ)))))))

(defn solve [file]
  (let [[before after] (->> (input->str file)
                            (map #(re-seq #" [A-Z] " %))
                            (map #(map clojure.string/trim %))
                            parse)]
    {:part (time (reduce str (walk before after)))
     :part2 (time "")}))
