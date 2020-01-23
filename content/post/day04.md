+++
title = "Day 04: Repose Record"
author = ["Piotr Kaznowski"]
date = 2020-01-18T18:22:00+01:00
tags = ["some", "val", "key"]
draft = false
weight = 104
summary = "Parsing with regexes, loops with hash-maps; TDDed"
+++

## Puzzle summary {#puzzle-summary}

<https://adventofcode.com/2018/day/4>

As input we get list of strings like `"[1518-03-10 23:57] Guard #73 begins shift"` or `"... falls asleep"` / `"... wakes up"`. The list should be sorted and parsed to get information about amount of minutes of each guard was asleep. Then we need to find the guard which was asleep the most and find the minute on which he was statistically asleep the most. Puzzle answer is guard's ID multiplied by the chosen minute.

For part 2 we need to find the guard which has the highest occurence of one minute during which he was asleep. Answer is counted in the same way as in part 1.


## Solution {#solution}

[Check full solution in the repo](https://gitlab.com/pkaznowski/aoc18/blob/master/src/aoc18/puzzle04.clj)


### Parsing the logs {#parsing-the-logs}

Let's grab all needed data (namely minute and, optionally, Guard's ID) using one func which should work like this:

<a id="code-snippet--day04-parse-test"></a>
```clojure
(deftest parse-test
  (testing "parse data and return hash-map with minutes and id if present"
    (is (= '(57 73) (parse "[1518-03-10 23:57] Guard #73 begins shift")))
    (is (= '(22 nil) (parse "[1518-03-11 00:22] wakes up")))))
```

The func should parse input with simple regex, than take searched groups (this is why I use `rest` because first group is the whole matched phrase) and convert matched strings to integers:

<a id="code-snippet--day04-parse"></a>
```clojure
(defn parse [log]
  (->> log
       (re-find #":(\d+)] (?:Guard #(\d+)|.)")
       rest
       (map #(if % (Integer/parseInt %) nil))))
```

Next, we need to process all log entries to get full info about each guard.
To accomplish that I will reduce all inputs using a `hash-map` where keys would be guards' ids and vals would be all minutes gathered from logs. The func should behave like that:


### Get all data into a managable structure {#get-all-data-into-a-managable-structure}

<a id="code-snippet--day04-check-in-test"></a>
```clojure
(deftest check-in-test
  (testing "should return hash-map with ids and minutes"
    (is (= {10 [5 25 30 55 24 29], 99 [40 50 36 46 45 55]}
           (check-in (input->str "day04-ex"))))))
```

(Where input is taken from the puzzle exemple.)

<a id="code-snippet--day04-check-in"></a>
```clojure
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
```


### Helper: vec of ints to vec of 2-el-lists {#helper-vec-of-ints-to-vec-of-2-el-lists}

Because I process only one input at a time, I don't know hom many minute ranges there will be for each guard. To fix output we need a simple helper which will convert vector of minutes to vector of lists containing falling asleep and waking up minute:

```clojure
(deftest get-ranges-test
  (testing "should split vector of ints into a vector of lists - pairs"
    (is (= ['(1 2) '(3 4) '(5 6)] (get-ranges [1 2 3 4 5 6])))))
```

I will loop over vector of minutes taking one pair each time until the list is exhausted:

<a id="code-snippet--day04-ranges"></a>
```clojure
(defn get-ranges [minutes]
  (loop [pair (take 2 minutes)
         rest (drop 2 minutes)
         vec []]
    (if (empty? pair)
      vec
      (recur (take 2 rest) (drop 2 rest) (conj vec pair)))))
```


### Helper: count sleepy minutes {#helper-count-sleepy-minutes}

Once we've got data ready to process we need to count minutes in given ranges. I will store minutes in a `hash-map` where keys are minutes and vals are occurences of a given minute throughout all logs of a guard:

<a id="code-snippet--day04-count-sleepy-test"></a>
```clojure
(deftest count-sleepy-test
  (testing "should return hash-map of minutes and their occurences"
    (is (= {1 1, 2 2, 3 2, 4 1, 5 1}
           (count-sleepy [1 4 4 6 2 4 ])))))
```

First I process minutes' vec with `get-ranges` than I cast the vec of ranges to double reduce func which will convert vec of sleep ranges into a range of numbers which will be used to update `hash-map` of all counted minutes (if given minute is already in the map, increase the counter by one, otherwise update the value for this minute with value of 1.

<a id="code-snippet--day04-count-sleepy"></a>
```clojure
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
```


### Process each guard data {#process-each-guard-data}

To solve part 1 we need info about sum of minutes slept by each guard and the minute in which the guard was sleeping the most.

```clojure
(deftest process-guard-data-test
  (testing "should return hash-map with keys :minute :occurence and :sleep-time"
    (is (= '({:minute 24, :occurence 2, :sleep-time 50}
             {:minute 45, :occurence 3, :sleep-time 30})
           (map process-guard-data
                [[5 25 30 55 24 29]
                 [40 50 36 46 45 55]])))))
```

Having all minutes stored in a `hash-map` we need only to apply `max` and `+` on values of each guard's map to get the most sleepy minute and all minutes slept respectively. Then we need to find the minute which had the highest occurence count.

<a id="code-snippet--day04-find-max-min"></a>
```clojure
(defn process-guard-data [minutes]
  (let [counted (count-sleepy minutes)
        minute_count (apply max (vals counted))
        sum (apply + (vals counted))]
    (some
     #(when (= (val %) minute_count)
        {:minute (key %) :occurence minute_count :sleep-time sum})
     counted)))
```


### Convert logs into vec of maps {#convert-logs-into-vec-of-maps}

Now we can start to put all pieces together and get the results.
First we have to process all the logs to get a `vec` of `maps` with info about guard `:id` and processed sleep times.

For puzzle example data we should get result like this:

<a id="code-snippet--day04-process-logs-test"></a>
```clojure
(deftest process-logs-test
  (testing "should meet puzzle 4 example data"
    (is (= [{:id 10, :minute 24, :occurence 2, :sleep-time 50}
            {:id 99, :minute 45, :occurence 3, :sleep-time 30}]
           (process-logs (check-in (input->str "day04-ex")))))))
```

<a id="code-snippet--day04-process-logs"></a>
```clojure
(defn process-logs [logs]
  (reduce
   (fn [guards [id minutes]]
     (conj guards (merge {:id id} (process-guard-data minutes))))
   [] logs))
```


### Find guard satisfying certain criteria {#find-guard-satisfying-certain-criteria}

To solve part 1 we need to find the guard with the longes sleep time, while to solve part 2 we need a guard which has highest frequency of one minute slept. Let's put it into one func which will find a guard using given criterium:

<a id="code-snippet--day04-find-guard-with-most"></a>
```clojure
(defn find-guard-with-most [what? logs]
  (reduce (fn [prev guard]
            (if (> (guard what?) (prev what?))
              guard prev))
          {:id nil :minute nil :occurence 0 :sleep-time 0}
          logs))
```


### Getting the results {#getting-the-results}

We are asked to return id number of chosen guard multiplied by the most slept minute:

<a id="code-snippet--day04-result"></a>
```clojure
(defn result [{:keys [id minute]}]
  (* id minute))
```

Let's put everything together (it appeared that there are virtuous guards who never sleep on duty, so we need to filter logs with `#(seq (last %)))` which will exclude empty data):

<a id="code-snippet--day04-solve"></a>
```clojure
(defn solve [input]
  (let [logs (->> input
                  input->str
                  sort
                  check-in
                  (filter #(seq (last %)))
                  process-logs)]
    {:part1 (time (result (find-guard-with-most :sleep-time logs)))
     :part2 (time (result (find-guard-with-most :occurence logs)))}))
```

Let's check if our solution passes the example data.

<a id="code-snippet--day04-example-test"></a>
```clojure
(deftest example-test
  (testing "should return 240 for the first part, and 4455 fot the second"
    (is (= {:part1 240 :part2 4455} (solve "day04-ex")))))
```

Run all tests:

```text

Testing aoc18.puzzle04-test
"Elapsed time: 0.01095 msecs"
"Elapsed time: 0.011563 msecs"

Ran 7 tests containing 8 assertions.
0 failures, 0 errors.
```

Get the answer:

```clojure
"Elapsed time: 0.138526 msecs"
"Elapsed time: 1.7591 msecs"
{:part1 5, :part2 4}
```
