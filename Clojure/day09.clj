(ns day09
  (:require [input :refer [f->nums]]))

(defn game [players lm]
  (let [sc (vec (repeat players 0))]
    (loop [m 1, circ [{:p 0 :n 0 :v 0}], cur 0, sc sc]
      (cond (> m lm) (apply max sc)
            (= 0 (mod m 23))
            (let [{:keys [p n v]} (nth (iterate #(circ (:p %)) (circ cur)) 7)
                  s (update sc (mod m players) + m v)
                  c (-> circ (assoc-in [n :p] p) (assoc-in [p :n] n) (conj nil))]
              (recur (inc m) c n s))
            :else
            (let [p (:n (circ cur)), n (:n (circ p))
                  c (-> circ (assoc-in [p :n] m) (assoc-in [n :p] m) (conj {:p p :n n :v m}))]
              (recur (inc m) c m sc))))))

(defn -main [day]
  (let [[players last-marble] (f->nums day), solve (partial game players)]
    {:part1 (solve last-marble) :part2 (solve (* 100 last-marble))}))


(comment
  (let [test-input [[[10 1618] 8317] [[13 7999] 146373] [[17 1104] 2764]
                    [[21 6111] 54718] [[30 5807] 37305]]]
    (every? true? (for [[[p n] e] test-input] (= e (game p n)))))
  )
