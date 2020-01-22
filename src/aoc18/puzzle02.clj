(ns aoc18.puzzle02
  (:require [aoc18.utils :refer [input->str]]
            [clojure.set :as set :refer [difference]]
            [clojure.string :as string :refer [replace join] :rename {replace rpl}]
            [clojure.repl :refer [doc]]))

(defn letter-repetition [word] ((comp set vals frequencies) word))

(defn two-threes [freqs] (vec (map #(if (freqs %) 1 0) [2 3])))

(defn checksum [words]
  (->> words
       (map (fn [w] (two-threes (letter-repetition w))))
       (apply map +)
       (apply *)))

(defn compare-letters [w1 w2] (frequencies (map = (seq w1) (seq w2))))

(defn find-similar [words]
  (first
   (for [w1 words
         :let [m (filter (fn [w2] (= ((compare-letters w1 w2) false) 1)) words)]
         :when (seq m)]
     (conj m w1))))

(defn common-str [pair]
  (string/replace (first pair)
               ((comp re-pattern str first)
                (->> pair
                     (map set)
                     (apply set/difference)))
               ""))

(defn common-str2 [[w1 w2]]
  (string/join (map (fn [l1 l2] (if (= l1 l2) l1)) (seq w1) (seq w2))))

(defn solve []
  (let [inp (input->str "day02")
        words (find-similar inp)]
    {:part1 (time (checksum inp))
     :part2-1 (time (common-str words))
     :part2-2 (time (common-str2 words))}))
