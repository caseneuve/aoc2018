(ns aoc18.puzzle03
  (:require [aoc18.utils :refer [input->str]]
            [clojure.set :refer [difference union]]))

(defn parse-claim
  "Returns a map where keys are claim id, x, y, width and height"
  [claim]
  (->> claim
       (re-seq #"\d+")
       (map #(Integer/parseInt %))
       (zipmap [:id :x :y :width :height])))

(defn covered-by
  "Returns all points [x y] covered by rectangle RECT"
  [{:keys [x y width height]}]          ; unpack only needed values
  (for [xx (range x (+ x width))
        yy (range y (+ y height))]
    [xx yy]))

(defn update-seen
  "Take old value and assign to 'id' var value of :id key in passed arg. Magic!"
  [old {id :id}]
  (if (some? old) (conj old id) #{id}))

(defn loop-overlapping
  "Checks all points covered by rectangle RECT, updating overlapping points and claims"
  [s rect]
  (loop [[xy & rest] (covered-by rect)
         seen s]
    (if (empty? xy)
      seen
      (recur rest (update seen xy update-seen rect)))))

(defn reduce-overlapping
  "Checks all points covered by rectangle RECT, updating overlapping points and claims"
  [claims]
  (reduce (fn [seen rect]
            ; old is passed automatically?
            (reduce (fn [seen xy] (update seen xy update-seen rect)) 
                    seen (covered-by rect)))
          {} claims))

(defn solve
  "First we parse  input data with regexes  making seq of vectors  mapped with appripriate
  keys.
  Funcs `reduce-overlapping' and `loop-overlapping' return dict where keys are positions
  on the xy grid and values are sets of rectangle ids.
  Part 1: to get all overlap positions we have to find all points which are claimed at
  least by two rectangles.
  Part 2: to find the one exclusively non overlapping rectangle we have to find difference
  between all ids and the set of ids of all points which are claimed by more than one
  rectangle."
  [file]
  (let [input (map parse-claim (input->str file))
        ;; claims (vals (reduce loop-overlapping {} input))
        claims (time (vals (reduce-overlapping input)))]
    {:part1 (time (->> claims
                       (map count)
                       (filter #(>= % 2))
                       count))
     :part2 (time (first
                   (difference
                    (->> input (map :id) set)
                    (->> claims
                         (filter #(> (count %) 1))
                         (apply union)))))}))
