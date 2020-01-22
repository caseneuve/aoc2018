+++
title = "Day 01: Chronal Calibration"
author = ["Piotr Kaznowski"]
date = 2020-01-10T10:55:00+01:00
tags = ["apply", "loop", "cycle", "reductions"]
draft = false
weight = 101
summary = "Apply, loop and reductions"
+++

## Puzzle summary {#puzzle-summary}

<https://adventofcode.com/2018/day/1>

As input we get integers representing change of "frequency". To count final frequency we need to add all changes starting from 0. For example changes notes as `+1, -2, +3, +1` would give final frequency `3`.


## Solutions {#solutions}

[Check full solution in the repo](https://gitlab.com/pkaznowski/aoc18/blob/master/src/aoc18/puzzle01.clj)


### Namespace {#namespace}

Since I would always use some helpers to read input, I will further on export `namespace` definition only when other requirements are needed.

<a id="code-snippet--day01-ns"></a>
```clojure
(ns aoc18.puzzle01 (:require [aoc18.utils :refer [input->ints]]
                             [clojure.repl :refer [doc]]))
```

<a id="code-snippet--day01-test-ns"></a>
```clojure
(ns aoc18.puzzle01-test
  (:require [clojure.test :refer :all]
            [aoc18.puzzle01 :refer [find-repeated-freq]]))
```


### Find repeated requency (part 2) {#find-repeated-requency--part-2}

Since part 1 consists on merely applying... `apply` to the collection of integers let's skip to part 2 immediately.

The task is to find first frequency which is reached twice. We would cycle through the input putting each frequency to a `hash-map`. If given frequency is already there, that'd be solution, if not, we have to loop more.

First I had to understand the `loop` form in Clojure. Recursion, as it seems, is not necessarily implemented through invoking the function in it's declaration but by using `recur` special form inside a loop. Loop takes arguments which and exactly the same structure of args has to be passed to recur.

In our loop we need input which will be cycled and concecutive frequencies counted by adding next change to the current frequency starting with 0. To implement simple loop operating on consecutive elements of a seq we may use destructing notation which takes first element and the rest from a seq, like this. Then, if certain condition is not satisfied we would recur passing the rest or return value:

<a id="code-snippet--day01-loop-example"></a>
```clojure
(loop [[first_el & the_rest] [0 1 2 3 4]
       increased_by_1 []]
  (if first_el
    (recur the_rest (conj increased_by_1 (inc first_el)))
    increased_by_1))   ;; => [1 2 3 4 5]
```

Let's apply this to our case:

<a id="code-snippet--day01-find-repeated-freq"></a>
```clojure
(defn find-repeated-freq [input]
  (loop [[freq & rest] (reductions + (cycle input))
         seen #{0}]
    (if (contains? seen freq)   ; we might write (seen freq) as well
      freq
      (recur rest (conj seen freq)))))
```

This func should behave like this:

<a id="code-snippet--day01-find-repeated-freq-test"></a>
```clojure
(deftest find-repeated-freq-test
  (testing "should match exemplary data"
    (is (= 0  (find-repeated-freq [1 -1])))
    (is (= 10 (find-repeated-freq [3 3 4 -2 -4])))
    (is (= 5  (find-repeated-freq [-6 3 8 5 -6])))
    (is (= 14 (find-repeated-freq [7 7 -2 -7 -4])))))
```

`reductions` is where magic happens. Let's look at the [docs](https://clojuredocs.org/clojure.core/reductions):

```text
-------------------------
clojure.core/reductions
([f coll] [f init coll])
  Returns a lazy seq of the intermediate values of the reduction (as
  per reduce) of coll by f, starting with init.
```

So `reductions` give us "snapshots" of consecutive moves of `reduce`. For example reduceing integers from 0 to 4 with sum (`+`) would give `10`. Using reductions would return seq of every step, namely:

1.  `0`,
2.  `0 + 1 = 1`,
3.  `1 + 2 = 3`,
4.  `3 + 3 = 6`,
5.  `6 + 4 = 10`.

<!--listend-->

<a id="code-snippet--day01-reductions-example"></a>
```clojure
(vec (reductions + (range 5))) ;; => [0 1 3 6 10]
(reduce + (range 5))           ;; => 10
```


### Putting things together {#putting-things-together}

Our solution will be as follows:

<a id="code-snippet--day01-solve"></a>
```clojure
(defn solve []
  (let [input (input->ints "day01")]
    {:part1 (time (apply + input))
     :part2 (time (find-repeated-freq input))}))
```

Finally let's run tests:

```text

Testing aoc18.puzzle01-test

Ran 1 tests containing 4 assertions.
0 failures, 0 errors.
```

And get the answer:

```text
"Elapsed time: 0.506288 msecs"
"Elapsed time: 205.237286 msecs"
{:part1 595, :part2 80598}
```
