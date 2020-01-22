+++
title = "Day 02: Inventory Management System"
author = ["Piotr Kaznowski"]
date = 2020-01-11T10:55:00+01:00
tags = ["comp", "frequencies", "vals-keys", "for-loop"]
draft = false
weight = 102
summary = "Frequencies, sets and list comprehension (for loop)"
+++

## Puzzle summary {#puzzle-summary}

<https://adventofcode.com/2018/day/2>

As input we get list of strings made of random letters representing IDs of boxes. The task is to find "checksum" of strings which contain some letters twice multiplied by count of strings which contain triple letters.


## Solution {#solution}

[Check full solution in the repo](https://gitlab.com/pkaznowski/aoc18/blob/master/src/aoc18/puzzle02.clj)


### Namespace and requirements {#namespace-and-requirements}

This time I'll explore some `set` and `string` funcs. (I add `:rename` keyword because Clojure complains about renaming `replace` from `clojure.core` by the func of same name from `clojure.string.`)

<a id="code-snippet--day02-ns"></a>
```clojure
(ns aoc18.puzzle02
  (:require [aoc18.utils :refer [input->str]]
            [clojure.set :as set :refer [difference]]
            [clojure.string :as string :refer [replace join] :rename {replace rpl}]
            [clojure.repl :refer [doc]]))
```


### Helper: count letter repetitions {#helper-count-letter-repetitions}

First let's count what are letter repetition ratios in given strings. If a string has no repeated letters our func should return a `set` containing 1 which indicates that all letters are used only once. If there are letters repeaded once, set should contain 2 as well, etc.:

<a id="code-snippet--day03-letter-repetition-test"></a>
```clojure
(deftest letter-repetition-test
  (testing "should return set of letter frequencies"
    (is (= #{1} (letter-repetition "abcdef")))
    (is (= #{1 2} (letter-repetition "abcabe")))
    (is (= #{1 2 3} (letter-repetition "abcabea")))))
```

<a id="code-snippet--day02-letter-repetition"></a>
```clojure
(defn letter-repetition [word] ((comp set vals frequencies) word))
```

Let's break it down.

`comp` enables making "composite" funcs instead of writing nested calls. For example:

<a id="code-snippet--day02-comp-example"></a>
```clojure
(assert (= (set (vals (frequencies "noob")))      ;; => #{1 2}
           ((comp set vals frequencies) "noob"))) ;; => #{1 2}
```

Next, `frequencies`:

```text
-------------------------
clojure.core/frequencies
([coll])
  Returns a map from distinct items in coll to the number of times
  they appear.
```

For example:

<a id="code-snippet--day02-frequencies-example"></a>
```clojure
(frequencies "noob")     ;; => {\n 1, \o 2, \b 1}
```

`vals`, as expected, would give seq of map's values (as opposite to `keys`):

<a id="code-snippet--day02-vals-example"></a>
```clojure
(vals {\n 1, \o 2, \b 1})  ;; => (1 2 1)
(keys {\n 1, \o 2, \b 1})  ;; => (\n \o \b)
```

And finally `set` "returns a set of the distinct elements of coll".


### Helper: doubles and triples {#helper-doubles-and-triples}

Since we are interested only in occurences of doubles and triples we may represent each string as a two-element `vector` where index 0 indicates doubles (0 if none, 1 if any) and index 1 indicates triples:

<a id="code-snippet--day02-two-threes-test"></a>
```clojure
(deftest two-threes-test
  (testing "should return vecor of ones and zeros, where indx 0 indicates if there are
            doubled letters, and idx 1 -- tripled letters"
    (is (= [0 0] (two-threes #{1})))
    (is (= [0 1] (two-threes #{1 3})))
    (is (= [1 0] (two-threes #{2 4})))
    (is (= [1 1] (two-threes #{1 2 3 4})))))
```

At first I wrote this using combined `if` statements put into `[]` but why not repeat oneself and use `map` for fun:

<a id="code-snippet--day02-two-threes"></a>
```clojure
(defn two-threes [freqs] (vec (map #(if (freqs %) 1 0) [2 3])))
```


### Count checksum (part 1) {#count-checksum--part-1}

Now we are ready to get checksum of all words. I will convert all words to vectors indicating doubles and triples, than multiply the sum of all doubles by the sum of all triples:

<a id="code-snippet--day02-checksum"></a>
```clojure
(defn checksum [words]
  (->> words
       (map (fn [w] (two-threes (letter-repetition w))))
       (apply map +)
       (apply *)))
```

Let's check if it matches exemplary data:

<a id="code-snippet--day02-checksum-test"></a>
```clojure
(deftest checksum-test
  (testing "should match puzzle 1 example"
    (is (= 12 (checksum (input->str "day02-ex1"))))))
```


### Helper: compare letters {#helper-compare-letters}

Since we have to find common letters in two words which differ exactly by one letter, first we need to find those two similar words.

For given two words I will convert them into sequences and `map` them checking if corresponding letters are equal. This will result in a seq of booleans. Feeding it to `frequencies` will result in a two-element map where boolenas are keys and their occurences are values. E.g. `{false 1 true 3}` means that in two words three letters are the same, but one letter in each word is not matched in another.

<a id="code-snippet--day02-part2"></a>
```clojure
(defn compare-letters [w1 w2] (frequencies (map = (seq w1) (seq w2))))
```

Let's see the code in action:

<a id="code-snippet--day02-compare-letters-test"></a>
```clojure
(deftest compare-letters-test
  (testing "should return hashmap where keys are booleans and values are number of common
            letters, e.g. {true 3, false 1} means that there are three letters doubled
            and one is not common"
    (is (= {false 4} (compare-letters "asdf" "qwer")))
    (is (= {true 3 false 1} (compare-letters "asdf" "asdq")))
    (is (= {true 2 false 2} (compare-letters "asdf" "askl")))))
```


### Find similar words {#find-similar-words}

To find two similar words in a collection I will use `for` loop using it's goodies of `:let` and `:when`. In fact it will act as a nested loop because for each word it will filter the whole collection looking for similar word (the `let` part) and if (or rather: `when`) theres a match it will return seq containing similar words. Since I know there will be only one pair it is safe to return the first element of the seq which is returned be the loop. I know it's not the most optimal solution, but for the sake of exploring new forms I'll stick with that for now.

**TODO**: make combinations of all words and using `loop` (which enables a "break" behavior) find two similar words.

<a id="code-snippet--day02-find-similar"></a>
```clojure
(defn find-similar [words]
  (first
   (for [w1 words
         :let [m (filter (fn [w2] (= ((compare-letters w1 w2) false) 1)) words)]
         :when (seq m)]
     (conj m w1))))
```

Let's check how it works:

<a id="code-snippet--day02-find-similar-test"></a>
```clojure
(deftest find-similar-test
  (testing "should return list of two words that differ only by one letter"
    (is (= '("fghij" "fguij") (find-similar (input->str "day02-ex2"))))))
```


### Get common string (part 2) {#get-common-string--part-2}

When we have two similar words found, getting common string may be accomplished by comparing two strings converted to `sets` of letters and replacing the letter which they differ by by an empty string. Or is it an overkill?

<a id="code-snippet--day02-common-str"></a>
```clojure
(defn common-str [pair]
  (string/replace (first pair)
               ((comp re-pattern str first)
                (->> pair
                     (map set)
                     (apply set/difference)))
               ""))
```

Second attempt: without using sets -- joining mapping of two words converted into sequencies in terms of identity of letters:

<a id="code-snippet--day02-common-str2"></a>
```clojure
(defn common-str2 [[w1 w2]]
  (string/join (map (fn [l1 l2] (if (= l1 l2) l1)) (seq w1) (seq w2))))
```

Now check if they are compatible:

<a id="code-snippet--day02-common-str-test"></a>
```clojure
(deftest example2-test
  (testing "should match puzzle2 example"
    (let [words (find-similar (input->str "day02-ex2"))]
      (is (= "fgij" (time (common-str words))))
      (is (= "fgij" (time (common-str2 words)))))))

;; the test above is tricky because all letters are already sorted alphabetically
;; we need to test words which have random order of letters

(deftest common-str-test
  (testing "should return the same string"
    (let [words '("waxyhi" "wexyhi")]
      (is (= "wxyhi" (time (common-str words))))
      (is (= "wxyhi" (time (common-str2 words)))))))
```


### Putting things together {#putting-things-together}

<a id="code-snippet--day02-solve"></a>
```clojure
(defn solve []
  (let [inp (input->str "day02")
        words (find-similar inp)]
    {:part1 (time (checksum inp))
     :part2-1 (time (common-str words))
     :part2-2 (time (common-str2 words))}))
```

Run tests:

```text

Testing aoc18.puzzle02-test
"Elapsed time: 0.047843 msecs"
"Elapsed time: 0.024114 msecs"
"Elapsed time: 0.045915 msecs"
"Elapsed time: 0.023275 msecs"

Ran 7 tests containing 16 assertions.
0 failures, 0 errors.
```

Get the answer:

```clojure
"Elapsed time: 7.029563 msecs"
"Elapsed time: 0.07834 msecs"
"Elapsed time: 0.02162 msecs"
{:part1 5456,
 :part2-1 "megsdlpulxvinkatfoyzxcbvq",
 :part2-2 "megsdlpulxvinkatfoyzxcbvq"}
```
