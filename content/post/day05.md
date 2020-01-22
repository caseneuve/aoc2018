+++
title = "Day 05: Alchemical Reduction"
author = ["Piotr Kaznowski"]
date = 2020-01-22T11:31:00+01:00
tags = ["peek", "pop", "remove", "queue"]
draft = false
weight = 105
summary = "Peek and pop: building a simple queue"
+++

## Puzzle summary {#puzzle-summary}

<https://adventofcode.com/2018/day/5>

As input we get string of 50.000 chars which is referred to as a "polymer". Polymer consists of units which correspond to letters. Units are of the same type when referring to the same letter of alphabet but differ by "polarity" when their case is different. Polymer reacts in the way that neighbor units of the same type and opposite polarity consume each other, namely string like `Aa` or `bB` would disappear. Our task is to find the lenght of the polymer after all reactions take place.

For example string `dabAcCaCBAcCcaDA` produces 10-char long polymer `dabCBAcaDA`.


## Solution {#solution}

[Check full solution in the repo](https://gitlab.com/pkaznowski/aoc18/blob/master/src/aoc18/puzzle05.clj)


### Helper: find matching letters {#helper-find-matching-letters}

Since reaction consists of "consuming" units of the same type but opposite polarity which means the same letters of opposite case we could use `Character/isUpperCase` to build a simple function `upper?` and look for two chars which reduced to the same case (by `clojure.string/lower-case` for example) are equal but differ cases. But this is somewhat tedious. Much simpler solution will be to convert chars to integers because this reduces whole logic to one calculation: checking if absolute difference between two ints is 32 (this being the difference between lowercase and uppercase letters represented by ints).

<a id="code-snippet--day04-chars-to-ints"></a>
```clojure
(prn (map char (range 97 123)))
(prn (map char (map #(- % 32) (range 97 123))))
```

```text
(\a \b \c \d \e \f \g \h \i \j \k \l \m \n \o \p \q \r \s \t \u \v \w \x \y \z)
(\A \B \C \D \E \F \G \H \I \J \K \L \M \N \O \P \Q \R \S \T \U \V \W \X \Y \Z)
```

This should return `nil` or throw an exception:

<a id="code-snippet--day05-assert"></a>
```clojure
(assert (= (- (int \a) (int \A)) 32))   ;; => nil
```

Good. So our helper func would look like this:

<a id="code-snippet--day05-match"></a>
```clojure
(defn match? [a b] (= (java.lang.Math/abs (- a b)) 32))
```

Let's test it:

<a id="code-snippet--day05-match-test"></a>
```clojure
(deftest match-test
  (testing "the same letters compared should return true, otherwise false"
    (is (= true  (reduce match? (map int [\A \a]))))
    (is (= true  (reduce match? (map int [\b \B]))))
    (is (= false (reduce match? (map int [\c \D]))))
    (is (= false (reduce match? (map int [\f \g]))))
    (is (= false (reduce match? (map int [\H \I]))))))
```


### React and count (1) {#react-and-count--1}

This kind of task looks like perfectly suited for functional approach. At the beginning I thought I would be very easy: we need to `reduce` original collection of chars to filtered one, where condition is our `match?` func. The logic: if last unit from new collection is matching current unit taken from the original one, return new collection without the last unit (using `butlast` func); if not, add current unit to the new collection -- produced polymer. But behavior wasn't as I'd expect, since Clojure, as it seems, implements different behaviors to different collections.

For example, look at `conj` docs:

```text
-------------------------
clojure.core/conj
([coll x] [coll x & xs])
  conj[oin]. Returns a new collection with the xs
    'added'. (conj nil item) returns (item).  The 'addition' may
    happen at different 'places' depending on the concrete type.
```

Let's emphasize this:

```text
The 'addition' may happen at different 'places' depending on the concrete type.
```

Only converting collections to `vectors` ensured expected output, but it was slooooow. Then I found some commentary which explained that one of the reasons to implement different behavior for different collections may be ability to build FIFOs or queues and, in fact, this is what I was looking for (I thought that `last`, `butlast` and `conj` would do the job). To implement this I had to pick differntly crafted func: `peek` and `pop`.

Let's look at [peek](https://clojuredocs.org/clojure.core/peek) docs:

```text
-------------------------
clojure.core/peek
([coll])
  For a list or queue, same as first, for a vector, same as, but much
  more efficient than, last. If the collection is empty, returns nil.
```

And [pop](https://clojuredocs.org/clojure.core/pop):

```text
-------------------------
clojure.core/pop
([coll])
  For a list or queue, returns a new list/queue without the first
  item, for a vector, returns a new vector without the last item. If
  the collection is empty, throws an exception.  Note - not the same
  as next/butlast.
```

I had to experiment with that a little to find out how exactly my code would behave, and than came out to this simple solution:

<a id="code-snippet--day05-react"></a>
```clojure
(defn react [polymer]
  (count
   (reduce
    (fn [coll unit]
      (if (and (seq coll) (match? (peek coll) unit))
        (pop coll)
        (conj coll unit)))
    '() polymer)))
```


### Find the shortest polymer (2) {#find-the-shortest-polymer--2}

Our task is to find reaction which produces the shortest polymer _after_ removing units of the same type (namely letters which differ only casewise). Since I've converted input string to integers now I have only to exclude pairs of integers where one is in the range from 97 to 122 and second is minus 32.

<a id="code-snippet--day05-remove-units"></a>
```clojure
(defn remove-units [coll unit] (remove (hash-set unit (- unit 32)) coll))
```

`remove` takes a predicate agains a collection. At first I wrote an explicit predicate which was combining arithmetical operations:

```text
#(or (= num %) (= (- num 32) %))
```

But since [sets may be functions](http://clojure-doc.org/articles/language/functions.html#sets-as-functions) we can use a `hash-set` as a predicate, as above.

Testing. `remove-units` operate on integers but let's use string and chars for readability sake:

<a id="code-snippet--day05-remove-units-test"></a>
```clojure
(deftest remove-units-test
  (testing "should remove ints corresponding to lower- and uppercase letters from the
            collection"
    (is (= [\o \l \a \n]
           (vec (map char (remove-units (map int "Golang") (int \g))))))
    (is (= [\C \l \o \j \u \r \e]
           (vec (map char (remove-units (map int "Clojure") (int \x))))))))
```


### Putting things together {#putting-things-together}

Solution to the second part consists of finding the shortest polymer after consecutively removing some units. I'd map `react` feed by polymer with removed units over range of available units (i.e. chars):

<a id="code-snippet--day05-solution"></a>
```clojure
(defn solve [input]
  (let [polymer (map int input)]
    {:part1 (time (react polymer))
     :part2 (time (apply min
                         (map #(react (remove-units polymer %))
                              (range 97 123))))
     }))
```

<a id="code-snippet--day05-remove-units-test"></a>
```clojure
(deftest solve-test
  (testing "should match examples"
    (is (= {:part1 10 :part2 4} (solve "dabAcCaCBAcCcaDA")))))
```

Run all tests:

```text

Testing aoc18.puzzle05-test
"Elapsed time: 0.623315 msecs"
"Elapsed time: 6.736692 msecs"

Ran 3 tests containing 8 assertions.
0 failures, 0 errors.
```

Get the answer:

```text
"Elapsed time: 416.582294 msecs"
"Elapsed time: 10419.124998 msecs"
{:part1 10972, :part2 5278}
```

It takes about 10 sec to complete the second part (without using queue it was taking too long) -- I'm curious how could I optimize it to work faster?
