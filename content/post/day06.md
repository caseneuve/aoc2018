+++
title = "Day 06: Day 6: Chronal Coordinates"
author = ["Piotr Kaznowski"]
date = 2020-01-03
draft = true
weight = 106
summary = "Optimizing with pmap; groupping with min-key, dissociating hash-maps and more!"
+++

## Puzzle summary {#puzzle-summary}

<https://adventofcode.com/2018/day/6>

This time we get set of x, y coordinates as input. We have to put those coordinates into an infinite grid. Each coordinate has its area which is computed using Manhattan distance: every point closest to given coordinate is included to the area. When a point has equal distances to two (or more) coordinates it's not counted. If area of a coordinate is not limited by points adhering to anther area it's considered infinite.
Our task is to find largest non-infinite area on the grid.

**After-thoughts**
Ok, this one was not very pleasant.

1.  Clojure makes needed computations rather slowly (after optimizations it still tooks around 40 sec. for each part), I don't know how to make it faster.
2.  I misinterpreted first part's instruction assuming that only coordinates on grid's border should be excluded from consideration as those of infinite area (since the grid is infinite), it appears we have to exclude coordinates closest to the grid's border.

Anyway I had to look for some hints, and it was worth it since I discovered new stuff: `min-key`, `pmap`, `dissoc`, etc.


## Solution {#solution}


### Manhattan distance {#manhattan-distance}

The [formula](https://en.wikipedia.org/wiki/Taxicab%5Fgeometry#Formal%5Fdefinition) for...

```text
...the taxicab distance between (p1,p2) and (q1,q2) is |p1-q1|+|p2-q2|
```

We need to get the sum of absolute values of differences between xs and ys:

<a id="code-snippet--day06-manhattan-test"></a>
```clojure
(deftest manhattan-test
  (testing "should return sum of absolute values of diffs of xs and ys"
    (is (= 2 (manhattan [1 0] [0 1])))))
```

That's straight forward:

<a id="code-snippet--day06-manhattan"></a>
```clojure
(defn manhattan [[x y] [x* y*]]
  (+ (java.lang.Math/abs (- x x*)) (java.lang.Math/abs (- y y*))))
```


### Find closest point {#find-closest-point}

Given set of coordinates we may find closest coordinate to any given point. Using given example:

```text
aaaaa.cccc
aAaaa.cccc
aaaddecccc
aadddeccCc
..dDdeeccc
bb.deEeecc
bBb.eeee..
bbb.eeefff
bbb.eeffff
bbb.ffffFf
```

our func should return `A` (1, 1) for point `4, 0`; `nil` for point `5, 0` since it's equally distant from points `A` and `C`; `D` (3, 4) for point `4, 2` and `E` (5, 5) for point `5, 2`:

```clojure
(deftest closest-test
  (testing "should match exemplary data"
    (let [coords (input->xy "day06-ex")]
      (is (= [1 1] (closest coords [5 0])))
      (is (= nil   (closest coords [6 0])))
      (is (= [3 4] (closest coords [4 2])))
      (is (= [5 5] (closest coords [5 2]))))))
```

OK, first we need to create a `hash-map` associating distances with coordinates. To accomplish that I'll use `group-by`:

```text
-------------------------
clojure.core/group-by
([f coll])
  Returns a map of the elements of coll keyed by the result of
  f on each element. The value at each key will be a vector of the
  corresponding elements, in the order they appeared in coll.
```

So, given point `4, 0` from the [example](#org815a9a3) we will get a hash-map where distances are `keys` (results of `mahnattan` func fed by the point and coordinate), and `values` are coordinates compared to the point:

<a id="code-snippet--day06-group-by-example"></a>
```clojure
(group-by #(manhattan [4 0]  %) (input->xy "day06-ex"))
```

```clojure
{4 [(1 1)], 9 [(1 6)], 7 [(8 3)], 5 [(3 4)], 6 [(5 5)], 13 [(8 9)]}
```

We are interested in the closest coordinate, so we need to filter the `hash-map` by keys but return the value corresponding to the minimal key. The tool for this is `min-key`:

```text
-------------------------
clojure.core/min-key
([k x] [k x y] [k x y & more])
  Returns the x for which (k x), a number, is least.

  If there are multiple such xs, the last one is returned.
```

`k` from the documentation denotes a func (?) which will be fed by other arguemnts of `min-key`, for example:

<a id="code-snippet--day06-min-key-example"></a>
```clojure
(min-key first [9 1] [2 7])             ;;=> [2 7]
(min-key #(first (keys %)) {9 1} {2 7}) ;;=> {2 7}
```

Since output of our [example](#code-snippet--day06-group-by-example) is a `hash-map` we need to `apply` `min-key` to the whole using `key` a filtering func:

<a id="code-snippet--day06-min-key-over-example"></a>
```clojure
;;  (apply min-key key (group-by #(manhattan [4 0]  %) (input->xy "day06-ex")))
  (apply min-key key (group-by #(manhattan [4 0]  %) (input->xy "day06-ex")))
```


### Putting things together {#putting-things-together}

```text

Testing aoc18.puzzle06-test

Ran 2 tests containing 5 assertions.
0 failures, 0 errors.
```
