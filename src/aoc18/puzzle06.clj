(ns aoc18.puzzle06 (:require [clojure.repl :refer [doc]]
                             [aoc18.utils :refer [input->xy]]))

(defn manhattan [[x y] [x* y*]]
  (+ (java.lang.Math/abs (- x x*)) (java.lang.Math/abs (- y y*))))

(defn closest [coords pos]
  (let [cand (val (apply min-key key (group-by #(manhattan pos %) coords)))]
    (when (= 1 (count cand)) (first cand))))

(defn make-grid [[minx miny] [maxx maxy]]
  (for [x (range minx (inc maxx)) y (range miny (inc maxy))] [x y]))

(defn make-border [[minx miny] [maxx maxy]]
  (apply concat (concat (for [x (range minx (inc maxx))] (list [x miny] [x maxy]))
                        (for [y (range miny (inc maxy))] (list [minx y] [maxx y])))))

(defn sum-of-distances [coords pos] (apply + (map (partial manhattan pos) coords)))

(defn solve [file]
  (let [coords (input->xy file)
        minis (reduce #(map min %1 %2) [##Inf ##Inf] coords)
        maxis (reduce #(map max %1 %2) [0 0] coords)
        grid (make-grid minis maxis)
        border (make-border minis maxis)]
    {:part1 (time
             (let [infinites (->> border (keep (partial closest coords)) set)
                   areas (->> grid
                              (pmap (partial closest coords))
                              (filter some?)
                              frequencies)]
               (->> infinites
                    (reduce dissoc areas)
                    vals
                    (reduce max))))
     :part2 (time
             (->> grid
                  (pmap (partial sum-of-distances coords))
                  (filter #(< % 10000))
                  count))
     }))

