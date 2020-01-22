+++
title = "Day 03: No Matter How You Slice It"
author = ["Piotr Kaznowski"]
date = 2020-01-12T21:04:00+01:00
tags = ["reduce", "update", "zipmap", "upacking", "re-seq"]
draft = false
weight = 103
summary = "Discovering magic of reduce, update and some syntactic sugars"
+++

## Puzzle summary {#puzzle-summary}

<https://adventofcode.com/2018/day/3>

Input is a list of strings like `#1 @ 393,863: 11x29` representing an elf's "claim" where `#1` represents claim's id, `393,863` represents points x, y of upper left vertex of a rectangle on the cartesian grid and `11x29` indicate width and height of the rectangle.

Our task is to find number of points on the grid covered potentially by the claims (part 1) and find the only one rectangle which does not overlap with others (part 2).


## Solution {#solution}

[Check full solution in the repo](https://gitlab.com/pkaznowski/aoc18/blob/master/src/aoc18/puzzle03.clj)


### Namespace and requirements {#namespace-and-requirements}

Besides of usual input parsing I will use `difference` and `union` from `clojure.set`.

<a id="code-snippet--day03-ns"></a>
```clojure
(ns aoc18.puzzle03
  (:require [aoc18.utils :refer [input->str]]
            [clojure.set :refer [difference union]]))
```


### Parsing the claim {#parsing-the-claim}

Claims look like `#123 @ 3,2: 5x4` and we are interested only in numbers and their orded. I'll parse each claim and put all numbers into a `hash-map` with keys `:id`, `:x`, `:y`, `:widht`
and `:height`.

```clojure
(deftest parse-claim-test
  (testing "Should return all numbers found in given str ordered by keys in a hash map"
    (is (= {:id 1 :x 393 :y 863 :width 11 :height 29}
           (parse-claim "#1 @ 393,863: 11x29")))))
```

Since we are interested only in numbers we can easily parse each claim using `re-seq` with simple regex `#\d+`. Then, after converting strings to integers we can zip those numbers with keys of map which we want to create using `zipmap` func:

<a id="code-snippet--day03-parse"></a>
```clojure
(defn parse-claim
  "Returns a map where keys are claim id, x, y, width and height"
  [claim]
  (->> claim
       (re-seq #"\d+")
       (map #(Integer/parseInt %))
       (zipmap [:id :x :y :width :height])))
```


### First attempt {#first-attempt}

At this point I tried to write a solution using `loop` form. As you can see it turned out to be quite long and complex (using three `recur` forms and nested `if` statements. It worked! But...

<a id="code-snippet--day03-old"></a>
```clojure
(defn count-overlapping
  "Checks all points covered by rectangle RECT, updating overlapping points and claims"
  [[s c o] rect]
  (loop [[xy & rest] (covered-by rect)
         seen s
         claims c
         overlapping o]
    (let [this (rect :id)
          other (seen xy)]
      (if (empty? xy)
        [seen claims overlapping]
        (if (some? other)
            (if (> (count other) 1)
              (recur rest seen (union claims other #{this}) overlapping)
              (recur rest
                     (assoc seen xy (conj other this))
                     (union claims other #{this})
                     (+ 1 overlapping)))
            (recur rest (conj seen {xy #{this}}) claims overlapping))))))


(defn solve []
  (let [input (map parse-claim (input->str "day03"))
        [_ overlapping-claims counter] (reduce count-overlapping [{} #{} 0] input)]
    {:part1 counter
     :part2 (first (difference (set (map :id input)) overlapping-claims))}))
```

... I realized that something is not right since the func returns data which I don't need and does some unnecessary computations storing part of the solution in a separate `counter` variable.

So I started to refactor by eliminating what was unnecessary and making the code more modular. I eventually could considerable shorten the `loop` form to only one `recur` using two short helper funcs: first getting area covered by a rectangle in terms of cartesian coordinates; second used just to update `hash-map` of all points ever covered by a claim.


### Helper: get area covered by a rectangle {#helper-get-area-covered-by-a-rectangle}

Having all claims transferred to managable data structure I need to get all points in the cartesian grid covered by given claim's rectangle. For example square with coordinates of upper left vertex `x = 1`, `y = 1` and width of 2 covers points `(1, 1)`, `(1, 2)`, `(2, 1)` and `(2, 2)`:

```clojure
(deftest covered-by-test
  (testing "Should return a seq of vectors containing x and y positions of a rectangle
            passed as an arg"
    (is (= '([1 1] [1 2] [2 1] [2 2])
           (covered-by {:id 1 :x 1 :y 1 :width 2 :height 2})))))
```

Since we need only certain values from a hash-map where we store the data, we may unpack them using some syntactic sugar while passing arguments. Thanks to that I won't have to write `let` form to unpack and bind values to temporary variables. Then a simple `for` loop will do:

<a id="code-snippet--day03-covered"></a>
```clojure
(defn covered-by
  "Returns all points [x y] covered by rectangle RECT"
  [{:keys [x y width height]}]          ; unpack only needed values
  (for [xx (range x (+ x width))
        yy (range y (+ y height))]
    [xx yy]))
```


### Helper: update seen points {#helper-update-seen-points}

This func will be used by `update` func later. What is worth noting here is argument `old`
which will be passed automatically by the `update` func. The second arg uses unpacking sugar assigning value of the `:id` key from map passed as arg to temporary variable `id`.
`some?` returns true if x is not nil, false otherwise.

<a id="code-snippet--day03-update-seen"></a>
```clojure
(defn update-seen
  "Take old value and assign to 'id' var value of :id key in passed arg. Magic!"
  [old {id :id}]
  (if (some? old) (conj old id) #{id}))
```

To see how this should work we have to put the func into the contex of `update`. I will check both cases of the desired behavior:

```clojure
(deftest update-seen-replaces-test
  (testing "Should update old value adding new"
    (let [before {"a" #{1}}
          after (update before "a" update-seen {:id 2})]
      (is (= #{1 2} (get after "a"))))))

(deftest update-seen-creates-test
  (testing "Should create new value because there was none"
    (let [before {}
          after (update before "a" update-seen {:id 3})]
      (is (= #{3} (get after "a"))))))
```


### Refactoring with loop {#refactoring-with-loop}

Finally I got this func which could be used with `redce` on parsed claims:

<a id="code-snippet--day03-loop"></a>
```clojure
(defn loop-overlapping
  "Checks all points covered by rectangle RECT, updating overlapping points and claims"
  [s rect]
  (loop [[xy & rest] (covered-by rect)
         seen s]
    (if (empty? xy)
      seen
      (recur rest (update seen xy update-seen rect)))))
```


### Further refactoring with reduce {#further-refactoring-with-reduce}

Then I found a similar solution which was using `reduce` instead of `loop` which was more convenient because it enabled further slimming down the code and, hmm, reducing more unnecessary data.

<a id="code-snippet--day03-reduce"></a>
```clojure
(defn reduce-overlapping
  "Checks all points covered by rectangle RECT, updating overlapping points and claims"
  [claims]
  (reduce (fn [seen rect]
            ; old is passed automatically?
            (reduce (fn [seen xy] (update seen xy update-seen rect))
                    seen (covered-by rect)))
          {} claims))
```

To compare both funcs we may put them into one test:

<a id="code-snippet--day03-loop-and-reduce-test"></a>
```clojure
(deftest loop-and-reduce-overlapping-test
  (testing "Should return hash map where keys are x, y positions and values are ids of
            rectangles covering those positions"
    (let [rects [{:id "a" :x 1 :y 1 :width 2 :height 2}
                 {:id "b" :x 2 :y 2 :width 2 :height 2}]
          seen {}
          expected {[1 1] #{"a"}
                    [2 1] #{"a"}
                    [1 2] #{"a"}
                    [2 2] #{"a" "b"}
                    [3 2] #{"b"}
                    [2 3] #{"b"}
                    [3 3] #{"b"}}]
      (is (= expected (reduce loop-overlapping seen rects)))
      (is (= expected (reduce-overlapping rects))))))
```


### Putting things together {#putting-things-together}

<a id="code-snippet--day03-solve"></a>
```clojure
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
```

Let's check if this works for exemplary data:

```clojure
(deftest example-test
  (testing "should return 4 for the first part and 3 for the second "
    (is (= {:part1 4 :part2 3}
           (solve "day03-ex")))))
```

Run tests:

```text

Testing aoc18.puzzle03-test
"Elapsed time: 0.143403 msecs"
"Elapsed time: 0.044249 msecs"
"Elapsed time: 0.183151 msecs"

Ran 6 tests containing 7 assertions.
0 failures, 0 errors.
```

Get the answer:

```text
"Elapsed time: 1164.283612 msecs"
"Elapsed time: 317.435826 msecs"
"Elapsed time: 320.920726 msecs"
{:part1 98005, :part2 331}
```
