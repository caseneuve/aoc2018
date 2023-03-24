(ns day18
  (:require [input :refer [f->str]]))

(defn show [g xy] (doseq [y (range xy) x (range xy)] (print (g [x y])) (when (= x (dec xy)) (prn))))

(defn parse [it]
  (->> it
       (reduce (fn [[[x y] g] c] (case c \newline [[0 (inc y)] g] [[(inc x) y] (assoc g [x y] c)]))
        [[0 0] {}])
       second))

(defn adjacent [grid [x y]]
  (frequencies
   (for [yy [(dec y) y (inc y)], xx [(dec x) x (inc x)] :when (not= [x y] [xx yy])]
     (grid [xx yy]))))

(defn magic [grid]
  (reduce
   (fn [new pos]
     (let [c (grid pos), a (adjacent grid pos)]
       (case c
         \. (cond-> new (>= (a \| 0) 3) (assoc pos \|))
         \| (cond-> new (>= (a \# 0) 3) (assoc pos \#))
         (cond-> new (or (nil? (a \#)) (nil? (a \|))) (assoc pos \.)))))
   grid (keys grid)))

;; part 2 heuristics: need to find repetitive cycle in this lumber-game-of-life
;; once we have it (by finding minute in which first repeated grid shows up -- m2
;; and subtracting from it the minute when the previous shown up -- m1), we only need
;; to find the reminder of (total-minutes - m2) divided by the cycle length (m1 - m2)
;; and update the grid for that reminder of time

(defn lumber-collection [it total-minutes]
  (loop [m 0, x total-minutes, grid it, seen {}]
    (if (= m x) (apply * (-> grid vals frequencies (dissoc \.) vals))
      (let [g (magic grid)]
        (if (contains? seen g)
          (recur 1 (rem (- x m) (- m (seen g))) g {})
          (recur (inc m) x g (assoc seen g m)))))))

(defn -main [day]
  (let [solve (partial lumber-collection (->> day f->str parse))]
    {:part1 (solve 10), :part2 (solve 1000000000)}))


(comment
  (let [test-input ".#.#...|#.
.....#|##|
.|..|...#.
..|#.....#
#.#|||#|#|
...#.||...
.|....|...
||...#|.#|
|.||||..|.
...#.|..|."]
    (= 1147 (-> test-input parse (lumber-collection 10))))
  )
