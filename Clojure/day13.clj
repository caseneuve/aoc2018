(ns day13
  (:require [clojure.string :refer [join]]))

(defn parse [it]
  (rest
   (reduce
    (fn [[[x y] tx cx] c]
      (cond
        (= c \newline)             [[0 (inc y)] tx cx]
        (contains? (set "v^<>") c) [[(inc x) y] tx (conj cx [[x y] ({\v [0 1] \^ [0 -1] \< [-1 0] \> [1 0]} c) :l])]
        (contains? (set "\\/+") c) [[(inc x) y] (assoc tx [x y] c) cx]
        :else                      [[(inc x) y] tx cx]))
    [[0 0] {} []] it)))

(def b-slash #(vec (reverse %)))
(def slash #({[1 0] [0 -1], [0 -1] [ 1 0], [-1 0] [0  1], [0  1] [-1 0]} %))
(def right #({[1 0] [0  1], [0  1] [-1 0], [-1 0] [0 -1], [0 -1] [ 1 0]} %))
(def left  #({[1 0] [0 -1], [0 -1] [-1 0], [-1 0] [0  1], [0  1] [ 1 0]} %))

(defn move [tracks [pos dir turn]]
  (let [np (mapv + pos dir)]
    (into [np] (case (tracks np)
                 \+ (case turn :l [(left dir) :s], :r [(right dir) :l], :s [dir :r])
                 \\ [(b-slash dir) turn], \/ [(slash dir) turn], [dir turn]))))

(defn run [tracks carts part]
  (let [rm (fn [p xs] (remove #(= (first %) p) xs))]
    (loop [c carts]
      (if (= 1 (count c)) (join "," (ffirst c))
          (recur (loop [[el & rs] c, nc []]
                   (if (empty? el) nc
                       (let [[e & _ :as x] (move tracks el)]
                         (if (some #{e} (concat (map first rs) (map first nc)))
                           (case part 1 (recur () [x]) (recur (rm e rs) (rm e nc)))
                           (recur rs (conj nc x)))))))))))

(defn -main [_]
  (let [solve (apply partial run (parse (slurp "inputs/day13")))]
    {:part1 (solve 1) :part2 (solve 2)}))


(comment
  (let [test-input [[1 "inputs/day13t1" "7,3"] [2 "inputs/day13t2" "6,4"]]]
    (for [[p it e] test-input :let [[t c] (parse (slurp it))]] (= e (run t c p))))
  )
