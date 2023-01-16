(ns day10
  (:require [input :refer [f->str]]))

(defn parse [it]
  (->> it (re-seq #"-?\d+") (map parse-long) (partition 4)))

(def minmax #(apply (juxt min max) %))

(defn seconds [it]
  (let [[mi ma] (minmax (map second it))
        step (->> it (keep (fn [[_ y _ d]] (when (= y mi) d))) first)]
    (-> (- mi ma) (quot (* 2 step)) abs)))

(defn display [it]
  (let [[[x X] [y Y]] (map minmax (apply map vector it))]
    (doseq [y (range y (inc Y)), x (range x (inc X))]
      (print (if (some #{[x y]} it) "█" " ")) (when (= x X) (prn)))))

(defn fast-forward [it s] (keep (fn [[x y a b]] [(+ x (* s a)) (+ y (* s b))]) it))

(defn -main [day]
  (let [input (->> day f->str parse), s (seconds input), message (fast-forward input s)]
    {:part1 (do (display message) "JJXZHKFP") :part2 s}))


(comment
  (let [test-input "position=< 9,  1> velocity=< 0,  2>
position=< 7,  0> velocity=<-1,  0>
position=< 3, -2> velocity=<-1,  1>
position=< 6, 10> velocity=<-2, -1>
position=< 2, -4> velocity=< 2,  2>
position=<-6, 10> velocity=< 2, -2>
position=< 1,  8> velocity=< 1, -1>
position=< 1,  7> velocity=< 1,  0>
position=<-3, 11> velocity=< 1, -2>
position=< 7,  6> velocity=<-1, -1>
position=<-2,  3> velocity=< 1,  0>
position=<-4,  3> velocity=< 2,  0>
position=<10, -3> velocity=<-1,  1>
position=< 5, 11> velocity=< 1, -2>
position=< 4,  7> velocity=< 0, -1>
position=< 8, -2> velocity=< 0,  1>
position=<15,  0> velocity=<-2,  0>
position=< 1,  6> velocity=< 1,  0>
position=< 8,  9> velocity=< 0, -1>
position=< 3,  3> velocity=<-1,  1>
position=< 0,  5> velocity=< 0, -1>
position=<-2,  2> velocity=< 2,  0>
position=< 5, -2> velocity=< 1,  2>
position=< 1,  4> velocity=< 2,  1>
position=<-2,  7> velocity=< 2, -2>
position=< 3,  6> velocity=<-1, -1>
position=< 5,  0> velocity=< 1,  0>
position=<-6,  0> velocity=< 2,  0>
position=< 5,  9> velocity=< 1, -2>
position=<14,  7> velocity=<-2,  0>
position=<-3,  6> velocity=< 2, -1>"
        input (->> test-input parse), s (seconds input)]
    (display (fast-forward input s))
    (= 3 s))
  )
