(defproject aoc18 "0.1.0-SNAPSHOT"
  :description "Advent of Code 2018 while learning Clojure"
  :url "https://pkaznowski.gitlab.io/aoc18"
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :main ^:skip-aot aoc18
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init (use 'aoc18.utils)})
