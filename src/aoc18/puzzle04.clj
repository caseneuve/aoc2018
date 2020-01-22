(ns aoc18.puzzle04 [:require [aoc18.utils :refer [input->str]]])

(defn parse [log]
  (->> log
       (re-find #":(\d+)] (?:Guard #(\d+)|.)")
       rest
       (map #(if % (Integer/parseInt %) nil))))

(defn check-in [logs]
  (first
   (reduce
    (fn [[guards last] log]
      (let [[m id] (parse log)]
        (if id
          [(update guards id (fn [old] (or old []))) id]
          [(update guards last (fn [old] (conj (or old []) m))) last])))
    [{} nil]
    logs)))

(defn get-ranges [minutes]
  (loop [pair (take 2 minutes)
         rest (drop 2 minutes)
         vec []]
    (if (empty? pair)
      vec
      (recur (take 2 rest) (drop 2 rest) (conj vec pair)))))

(defn count-sleepy [minutes]
  (->> minutes
       get-ranges
       (reduce
        (fn [counted sleep-range]
          (reduce
           (fn [counted minute]
             (update counted minute (fn [count] (if count (inc count) 1))))
           counted
           (apply range sleep-range)))
        {})))

(defn process-guard-data [minutes]
  (let [counted (count-sleepy minutes)
        minute_count (apply max (vals counted))
        sum (apply + (vals counted))]
    (some
     #(when (= (val %) minute_count)
        {:minute (key %) :occurence minute_count :sleep-time sum})
     counted)))

(defn process-logs [logs]
  (reduce
   (fn [guards [id minutes]]
     (conj guards (merge {:id id} (process-guard-data minutes))))
   [] logs)) 

(defn find-guard-with-most [what? logs]
  (reduce (fn [prev guard]
            (if (> (guard what?) (prev what?))
              guard prev))
          {:id nil :minute nil :occurence 0 :sleep-time 0}
          logs))

(defn result [{:keys [id minute]}]
  (* id minute))

(defn solve [input]
  (let [logs (->> input
                  input->str
                  sort
                  check-in
                  (filter #(seq (last %)))
                  process-logs)]
    {:part1 (time (result (find-guard-with-most :sleep-time logs)))
     :part2 (time (result (find-guard-with-most :occurence logs)))}))
