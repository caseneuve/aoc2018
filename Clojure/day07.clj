(ns day07
  (:require [input :refer [f->str]]))

(defn parse [it]
  (let [it (->> it (re-seq #" ([A-Z]) ") (map second) (map first))]
    (->> it (partition 2)
         (reduce
          (fn [[need waiting] [a b]] [(update need a conj b) (update waiting b inc)])
          [{} (-> it set (zipmap (repeat 0)))]))))

(defn available [waiting] (map first (filter #(= 0 (second %)) waiting)))

(defn busy-until [wait t step] [(- (+ (int step) wait t) (dec (int \A))) step])

(defn solve [[need waiting] wait workers]
  (let [bu (partial busy-until wait), init (take workers (available waiting))]
    (loop [t 0
           [[sec done] & q] (sort (map #(bu 0 %) init))
           waiting (reduce #(dissoc %1 %2) waiting init)
           order []]
      (if (nil? done) [(apply str order) t]
          (let [waiting (reduce #(update %1 %2 dec) (dissoc waiting done) (need done))
                steps (take (- workers (count q)) (sort (available waiting)))]
            (recur sec
                   (sort (concat q (map #(bu sec %) steps)))
                   (reduce #(dissoc %1 %2) waiting steps)
                   (conj order done)))))))

(defn -main [day]
  (let [input (->> day f->str parse)]
    {:part1 (first (solve input 0 1)) :part2 (second (solve input 60 5))}))


(comment
  (let [test-input "Step C must be finished before step A can begin.
Step C must be finished before step F can begin.
Step A must be finished before step B can begin.
Step A must be finished before step D can begin.
Step B must be finished before step E can begin.
Step D must be finished before step E can begin.
Step F must be finished before step E can begin."
        input (->> test-input parse)]
    {:1 (= "CABDFE" (first (solve input 0 1)))
     :2 (= ["CABFDE" 15] (solve input 0 2))})
  )
