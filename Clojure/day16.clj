(ns day16
  (:require [input :refer [f->str]]
            [clojure.string :refer [split]]))

(defn addr [rx ra rb rc] (assoc rx rc (+ (rx ra) (rx rb))))
(defn addi [rx ra vb rc] (assoc rx rc (+ (rx ra) vb)))
(defn mulr [rx ra rb rc] (assoc rx rc (* (rx ra) (rx rb))))
(defn muli [rx ra vb rc] (assoc rx rc (* (rx ra) vb)))
(defn banr [rx ra rb rc] (assoc rx rc (bit-and (rx ra) (rx rb))))
(defn bani [rx ra vb rc] (assoc rx rc (bit-and (rx ra) vb)))
(defn borr [rx ra rb rc] (assoc rx rc (bit-or (rx ra) (rx rb))))
(defn bori [rx ra vb rc] (assoc rx rc (bit-or (rx ra) vb)))
(defn setr [rx ra _  rc] (assoc rx rc (rx ra)))
(defn seti [rx va _  rc] (assoc rx rc va))
(defn gtir [rx va rb rc] (assoc rx rc (if (> va (rx rb)) 1 0)))
(defn gtri [rx ra vb rc] (assoc rx rc (if (> (rx ra) vb) 1 0)))
(defn gtrr [rx ra rb rc] (assoc rx rc (if (> (rx ra) (rx rb)) 1 0)))
(defn eqir [rx va rb rc] (assoc rx rc (if (= va (rx rb)) 1 0)))
(defn eqri [rx ra vb rc] (assoc rx rc (if (= (rx ra) vb) 1 0)))
(defn eqrr [rx ra rb rc] (assoc rx rc (if (= (rx ra) (rx rb)) 1 0)))

(def fns [addr addi mulr muli banr bani borr bori setr seti gtir gtri gtrr eqir eqri eqrr])

(defn parse [it]
  (let [[p1 p2] (map #(->> % (re-seq #"\d+") (map parse-long) (partition 4)) (-> it (split #"\n\n\n\n")))]
    [(partition 3 p1) p2]))

(defn three-or-more [it]
  (->> it
       (keep
        (fn [[br [o a b c] ar]]
          (when (> (count (for [f fns :when (= (f (vec br) a b c) (vec ar))] o)) 2) o)))
       count))

(defn opcodes [samples]
  (let [uniqs
        (fn [idx]
          (reduce
           (fn [acc [br [o a b c] ar]]
             (let [rs (for [i idx :when (= ((nth fns i) (vec br) a b c) (vec ar))] [o i])]
               (if (= (count rs) 1) (into acc rs) acc)))
           #{} samples))]
    (loop [idx (set (range (count fns))) ops (vec (repeat (count fns) nil))]
      (if (= 0 (count idx)) ops
          (let [[idx ops] (reduce (fn [[idx ops] [i n]] [(disj idx n) (assoc ops i n)]) [idx ops] (uniqs idx))]
            (recur idx ops))))))


(defn -main [day]
  (let [[p1 p2] (->> day f->str parse), ops (opcodes p1)]
    {:part1 (three-or-more p1)
     :part2 (->> p2 (reduce (fn [r [o a b c]] ((nth fns (ops o)) r a b c)) [0 0 0 0]) first)}))
