(ns day16
  (:require [input :refer [f->str]]
            [clojure.string :refer [split]]))

(def fns
  [(fn addr [rx ra rb rc] (assoc rx rc (+ (rx ra) (rx rb))))
   (fn addi [rx ra vb rc] (assoc rx rc (+ (rx ra) vb)))
   (fn mulr [rx ra rb rc] (assoc rx rc (* (rx ra) (rx rb))))
   (fn muli [rx ra vb rc] (assoc rx rc (* (rx ra) vb)))
   (fn banr [rx ra rb rc] (assoc rx rc (bit-and (rx ra) (rx rb))))
   (fn bani [rx ra vb rc] (assoc rx rc (bit-and (rx ra) vb)))
   (fn borr [rx ra rb rc] (assoc rx rc (bit-or (rx ra) (rx rb))))
   (fn bori [rx ra vb rc] (assoc rx rc (bit-or (rx ra) vb)))
   (fn setr [rx ra _  rc] (assoc rx rc (rx ra)))
   (fn seti [rx va _  rc] (assoc rx rc va))
   (fn gtir [rx va rb rc] (assoc rx rc (if (> va (rx rb)) 1 0)))
   (fn gtri [rx ra vb rc] (assoc rx rc (if (> (rx ra) vb) 1 0)))
   (fn gtrr [rx ra rb rc] (assoc rx rc (if (> (rx ra) (rx rb)) 1 0)))
   (fn eqir [rx va rb rc] (assoc rx rc (if (= va (rx rb)) 1 0)))
   (fn eqri [rx ra vb rc] (assoc rx rc (if (= (rx ra) vb) 1 0)))
   (fn eqrr [rx ra rb rc] (assoc rx rc (if (= (rx ra) (rx rb)) 1 0)))])

(defn parse [it]
  (let [[p1 p2] (map #(->> % (re-seq #"\d+") (map parse-long) (partition 4)) (split it #"\n\n\n"))]
    [(partition 3 p1) p2]))

(defn three-or-more [samples]
  (->> samples
       (reduce
        (fn [sum [br [o a b c] ar]]
          (cond-> sum (> (count (for [f fns :when (= (f (vec br) a b c) (vec ar))] o)) 2) inc))
        0)))

(defn opcodes [samples]
  (let [uniqs
        (fn [idx]
          (reduce
           (fn [acc [br [o a b c] ar]]
             (let [rs (for [i idx :when (= ((nth fns i) (vec br) a b c) (vec ar))] [o i])]
               (if (= (count rs) 1) (into acc rs) acc))) #{} samples))]
    (loop [[idx ops] [(set (range (count fns))) {}]]
      (if (empty? idx) ops
          (recur (reduce (fn [[idx ops] [i n]] [(disj idx n) (assoc ops i n)]) [idx ops] (uniqs idx)))))))

(defn -main [day]
  (let [[samples test] (->> day f->str parse), ops (opcodes samples)]
    {:part1 (three-or-more samples)
     :part2 (first (reduce (fn [r [o a b c]] ((nth fns (ops o)) r a b c)) [0 0 0 0] test))}))
