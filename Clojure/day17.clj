(ns day17
  (:require [input :refer [f->str]]))

;; debugging fns

(defn show [g]
  (let [[xmi xma] ((juxt #(apply min %) #(apply max %)) (map first  (keys g)))
        [_   yma] ((juxt #(apply min %) #(apply max %)) (map second (keys g)))]
    (doseq [y (range 0 (+ 2 yma)) x (range (dec xmi) (+ 2 xma))]
      (print ((assoc g [500 0] "0") [x y] " ")) (when (= x (inc xma)) (prn)))))

(defn show-frame [g [x y]]
  (let [r 60 n 4]
    (doseq [y' (range (- y r) (+ y (inc r))) x' (range (- x (* n r)) (+ x (inc (* n r))))]
      (print (g [x' y'] " ")) (when (= x' (+ (* n r) x)) (prn)))))

;; input

(defn parse [it]
  (->> it (re-seq #"\w=[^,\n]+") (partition 2) (map sort)
       (map (fn [xy] (map #(re-seq #"\d+" %) xy)))
       (reduce
        (fn [a [xx yy]]
          (let [[xb xe] (map parse-long xx), [yb ye] (map parse-long yy)]
            (apply merge a
                   (for [x (range xb (if xe (inc xe) (inc xb))) y (range yb (if ye (inc ye) (inc yb)))]
                     [[x y] "#"])))) {})))

;; solution

(defn fill [[x y :as xy] ground]
  (for [f [dec inc]]
    (loop [x' (f x), filled [xy]]
      (cond
        (= "#" (ground [x' y])) [true xy filled]
        (nil? (ground [x' (inc y)])) [false [x' y] (conj filled [x' y])]
        :else (recur (f x') (conj filled [x' y]))))))

(defn flow-on [ground]
  (let [[ymi yma] ((juxt #(apply min %) #(apply max %)) (map second (keys ground)))
        add-layer #(merge %1 (zipmap %2 (repeat %3)))]
    (loop [[x y :as pos] [500 0], forks clojure.lang.PersistentQueue/EMPTY, G ground]
      (cond
        (> y yma)                                               ; * out of the field:
        (if (empty? forks) G                                    ;   stop,
            (recur (peek forks) (pop forks) G))                 ;   or move to the remaining forks
        (= "|" (G [x (inc y)]))                                 ; * reaching a surface of an already filled pond from a different fork:
        (if (empty? forks) (assoc G pos "|")                    ;   stop,
            (recur (peek forks) (pop forks) (assoc G pos "|"))) ;   or move to the next fork
        (= "~" (G pos))                                         ; * already filled that pond (case: a fork within a pond),
        (recur (peek forks) (pop forks) G)                      ;   move to the next fork
        (some #{(G [x (inc y)])} ["#" "~"])                     ; * reaching either a 'bottom' of a pond or a partially filled one:
        (let [[[l? pos-l filled-l] [r? pos-r filled-r]] (fill pos G),
              add #(add-layer G (concat filled-l filled-r) %)]
          (cond                                                 ;   fill it:
            (and l? r?) (recur [x (dec y)] forks  (add "~"))    ;     # both sides are clay #
            l?          (recur pos-r       forks  (add "|"))    ;     one side stream <-
            r?          (recur pos-l       forks  (add "|"))    ;     one side stream ->
            :else (recur pos-l (conj forks pos-r) (add "|"))    ;     stream both sides, move left, remember fork on the right
            ))
        :else                                                   ; * stream down otherwise
        (recur [x (inc y)] forks (cond-> G (< ymi y) (assoc [x y] "|")))))))

(defn -main [day]
  (let [water (->> day f->str parse flow-on vals frequencies)
        solve #(->> % (select-keys water) vals (apply +))]
    {:part1 (solve ["|" "~"]) :part2 (solve ["~"])}))


(comment
  (let [test-input "x=495, y=2..7
y=7, x=495..501
x=501, y=3..7
x=498, y=2..4
x=506, y=1..2
x=498, y=10..13
x=504, y=10..13
y=13, x=498..504"
        w (->> test-input parse flow-on vals frequencies)
        solve #(apply + (vals (select-keys w %)))]
    {:1 (= 57 (solve ["|" "~"])), :2 (= 29 (solve ["~"]))})
  )
