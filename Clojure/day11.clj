(ns day11
  (:require [input :refer [f->nums]]))

(def size 300)
(def grid (for [y (range size) x (range size)] [x y]))

(defn power-lvl [sn [x y]]
  (let [id (+ (inc x) 10)]
    (-> y inc (* id) (+ sn) (* id) (quot 100) (rem 10) (- 5))))

(defn power-grid [it] (reduce #(assoc %1 %2 (power-lvl it %2)) {} grid))

(defn points [g [x y]]
  (if (or (= 0 x) (= 0 y)) g
      (update g [x y] + (g [x (dec y)]) (g [(dec x) y]) (- (g [(dec x) (dec y)])))))

(defn summed-table [it] (reduce points (power-grid it) grid))

(defn summed-area [t r [x y]]
  (- (+ (t [(dec (+ y r)) (dec (+ x r))] 0) (t [(dec y) (dec x)] 0))
     (t [(dec y) (dec (+ x r))] 0) (t [(dec (+ y r)) (dec x)] 0)))

(defn best-score [tbl s]
  (let [pts (for [y (range (- size s)) x (range (- size s)) :when (and (>= y (dec s)) (>= x (dec s)))]
              [(summed-area tbl s [x y]) [y x]])
        [sc p] (last (sort-by first pts))]
    [sc (conj (mapv inc p) s)]))

;; by watching results of `scores`, we see scores start to decline with squares bigger than 11x11
;; using this heuristic we may assume 11 is the answer for part 2
(defn scores [it beg end]
  (let [t (summed-table it)] (pmap (partial best-score t) (range beg (inc end)))))

(defn -main [day]
  (let [table (->> day f->nums first summed-table)
        solve (comp second (partial best-score table))]
    {:part1 (apply format "%d,%d" (solve 3))
     :part2 (apply format "%d,%d,%d" (solve 11))}))


(comment
    [(= '([33 45 3] [ 90 269 16]) (map second ((juxt first last) (scores 18 3 16))))
     (= '([21 61 3] [232 251 12]) (map second ((juxt first last) (scores 42 3 12))))]
  )
