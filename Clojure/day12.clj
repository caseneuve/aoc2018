(ns day12
  (:require [input :refer [f->lines lines]]))

(defn parse [lines]
  (let [[initial _ & rules] lines
        it (->> initial
                (drop (count "initial state: "))
                (zipmap (range))
                (keep (fn [[k v]] (when (= v \#) k))))
        rules (reduce #(assoc %1 (take 5 %2) (last %2)) {} rules)]
    [(set it) rules]))

(defn generation [rules it]
  (let [[mi ma] (apply (juxt min max) it)]
    (loop [i (- mi 2), new #{}]
      (if (= i (+ ma 2)) new
          (let [pat (for [x (range (- i 2) (+ i 3))] (if (contains? it x) \# \.))]
            (recur (inc i) (cond-> new (= (rules pat) \#) (conj i))))))))

;; Heuristics for part 2:
;; After 100th generation difference between sums is always 62 (at least in my input).

(defn -main [day]
  (let [[it rules] (parse (f->lines day)), genx (iterate (partial generation rules) it)]
    {:part1 (reduce + (nth genx 20))
     :part2 (reduce + (* (- 50000000000 100) 62) (nth genx 100))}))


(comment
  (let [test-input "initial state: #..#.#..##......###...###

...## => #
..#.. => #
.#... => #
.#.#. => #
.#.## => #
.##.. => #
.#### => #
#.#.# => #
#.### => #
##.#. => #
##.## => #
###.. => #
###.# => #
####. => #"
        [it rls] (-> test-input lines parse)]
    (= 325 (reduce + (nth (iterate (partial generation rls) it) 20))))
  )
