(ns day14)

(defn solve [n]
  (let [digits #(for [n (str %)] (- (byte n) 48)), nx (reverse (digits n)), c (count nx)
        pos #(mod (+ %1 (inc %2)) (count %3))
        part1  #(apply str (subvec % n (+ n 10)))]
    (loop [rcp [3 7], p1 0, p2 1, acc ()]
      (condp = nx
        (butlast acc) [(part1 rcp) (- (count rcp) c)]
        (rest acc)    [(part1 rcp) (- (count rcp) (inc c))]
        (let [v1 (rcp p1), v2 (rcp p2), new (digits (+ v1 v2)), rcp (into rcp new)
              p1 (pos p1 v1 rcp), p2 (pos p2 v2 rcp)]
          (recur rcp p1 p2 (take (inc c) (into acc new))))))))

(defn -main [_] (zipmap [:part1 :part2] (solve 793031)))
