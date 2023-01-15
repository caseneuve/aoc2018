(ns day08
  (:require [input :refer [f->nums nums]]))

(defn tree [[children meta & it]]
  (loop [[c m & lst :as it] it
         [to-go & children] (list children)
         [amount & next-meta :as meta] (list meta)
         [parent & next-parents :as parents] (list 0)
         node 1
         Tree {}]
    (cond (nil? c) Tree
          (= to-go 0) (recur (drop amount it)
                             children
                             next-meta
                             next-parents
                             (inc node)
                             (assoc-in Tree [parent :meta] (take amount it)))
          :else (recur lst
                       (conj children (dec to-go) c)
                       (conj meta m)
                       (conj parents node)
                       (inc node)
                       (update-in Tree [parent :children] conj node)))))

(defn metadata [tree] (->> tree vals (map :meta) (flatten) (apply +)))

(defn node-val [tree node]
  (let [{:keys [meta children]} (tree node), ch (vec (reverse children))]
    (->> (if (empty? ch) meta
             (for [i meta :let [node (get ch (dec i))] :when node] (node-val tree node)))
         (apply +))))

(defn -main [day]
  (let [input (->> day f->nums tree)]
    {:part1 (metadata input) :part2 (node-val input 0)}))


(comment
  (let [test-input "2 3 0 3 10 11 12 1 1 0 1 99 2 1 1 2", input (->> test-input nums tree)]
    {:1 (= 138 (metadata input)) :2 (= 66 (node-val input 0))})
  )
