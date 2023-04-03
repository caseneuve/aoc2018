(ns day19
  (:require [input :refer [f->lines]]))

(defn parse [[ip & ops]]
  [(->> ip last str parse-long)
   (->> ops (map #(->> % (re-seq #"\w+") (map (fn [x] (or (parse-long x) x))))) vec)])

(defn ops [rx [op a b c]]
  (assoc rx c
         (case op
           "addr" (+ (rx a) (rx b))
           "addi" (+ (rx a) b)
           "mulr" (* (rx a) (rx b))
           "muli" (* (rx a) b)
           "banr" (bit-and (rx a) (rx b))
           "bani" (bit-and (rx a) b)
           "borr" (bit-or (rx a) (rx b))
           "bori" (bit-or (rx a) b)
           "setr" (rx a)
           "seti" a
           "gtir" (if (> a (rx b)) 1 0)
           "gtri" (if (> (rx a) b) 1 0)
           "gtrr" (if (> (rx a) (rx b)) 1 0)
           "eqir" (if (= a (rx b)) 1 0)
           "eqri" (if (= (rx a) b) 1 0)
           "eqrr" (if (= (rx a) (rx b)) 1 0))))

;; Brute force solution to get part 1 and see how the program works

(defn -brute [ip insx]
  (loop [rx [0 0 0 0 0 0], n 0]
    (if (>= (rx ip) (count insx)) (first rx)
        (recur (-> rx (ops (insx (rx ip))) (update ip inc)) (inc n)))))

;; What the program does:

;; After initialization (steps 0, 17-35, 1-2), which sets the target number (T),
;; the program enters a series of loops which by incrementing multiplier (register 4)
;; unless it's bigger then T or the multiplicand (register 5) is a factor of T
;; (then it resets and increments the multiplicand), finds all factors of T
;; and stores their sum in register 0.  When finally multiplier and multiplcand
;; produce T, program exits returning the sum of all factors of T.

(defn run [ip insx init]
  (loop [rx [init 0 0 0 0 0]]
    (if (= (rx ip) 3)                   ; enter the program loop
      (loop [n 1, a 0]
        (cond (> n (rx 1)) a
              (zero? (mod (rx 1) n)) (recur (inc n) (+ a n))
              :else (recur (inc n) a)))
      (recur (-> rx (ops (insx (rx ip))) (update ip inc))) ; find T
      )))

(defn -main [day]
  (let [[ip inst] (->> day f->lines parse), solve (partial run ip inst)]
    {:part1 (solve 0) :part2 (solve 1)}))


(comment

  ;; 0 1 2 3 4 5
  ;; -----------
  ;; A B C D E F
  ;; -----------
  ;; 0 0 0 0 0 0

  ;; init.0
  ;; 0. addi 3 16 3 -> GOTO 17

  ;; init.3 p1 & p2:

  ;;  1. seti 1 8 5  -> p1: F = 1
  ;;  2. seti 1 0 4  -> p1: E = 1

  ;; after init: GOTO 3 (= enter LOOP)
  ;;     [A        B        C D E F]
  ;;      -------------------------
  ;; p1: [0      943      107 3 1 1]
  ;; p2: [0 10551343 10550400 3 1 1]

  ;; -------
  ;; LOOP:

  ;;  3. mulr 5 4 2  -> C = E * F
  ;;  4. eqrr 2 1 2
  ;;  5. addr 2 3 3

  ;; IF B == C {
  ;;   C = 1
  ;;   D = 5 + C => GOTO 7
  ;; } ELSE {
  ;;   C = 0
  ;;   D = 5 + 0 => GOTO 6 =>>> basically go back to 3 with incrementing E
  ;; }

  ;;  6. addi 3 1 3  => GOTO 8 (skip 7, increase E)

  ;;  7. addr 5 0 0  -> A = F + A
  ;;  8. addi 4 1 4  -> E = E + 1
  ;;  9. gtrr 4 1 2  -> IF (E > B) { C = 1 } ELSE { C = 0 }
  ;; 10. addr 3 2 3  -> D = 10 + C => IF (C == 0) { GOTO 11 } ELSE { GOTO 12 }
  ;; 11. seti 2 3 3  -> D = 2 => GOTO 3

  ;; 12. addi 5 1 5  -> F = F + 1
  ;; 13. gtrr 5 1 2  -> C = F + B
  ;; 14. addr 2 3 3  -> D = B + 14 => IF (B == 0) { GOTO 15 } ELSE { GOTO 16 == EXIT }
  ;; 15. seti 1 4 3  -> GOTO 2

  ;; END:
  ;; -------
  ;; 16. mulr 3 3 3

  ;; init.1

  ;; 17. addi 1 2 1  -> B = 2
  ;; 18. mulr 1 1 1  -> B = 4
  ;; 19. mulr 3 1 1  -> B = 19 * 4 = 76
  ;; 20. muli 1 11 1 -> B = 76 * 11 = 836
  ;; 21. addi 2 4 2  -> C = 4
  ;; 22. mulr 2 3 2  -> C = 4 * 22 = 88
  ;; 23. addi 2 19 2 -> C = 88 + 19 = 107
  ;; 24. addr 1 2 1  -> B = 836 + 107 = 943

  ;; p1 / p2:
  ;; 25. addr 3 0 3  -> p1: D = 25, p2 D = 26 => GOTO 27 (skip next)
  ;; 26. seti 0 7 3  -> p1: D = 0 => GOTO 1

  ;; p2 continues to set a bigger number:
  ;; 27. setr 3 2 2  -> C = D = 27
  ;; 28. mulr 2 3 2  -> C = C * 28 = 756
  ;; 29. addr 3 2 2  -> C = 29 + C = 785
  ;; 30. mulr 3 2 2  -> C = 30 * C = 23550
  ;; 31. muli 2 14 2 -> C = C * 14 = 329700
  ;; 32. mulr 2 3 2  -> C = C * 32 = 10550400
  ;; 33. addr 1 2 1  -> B = B + C = 10551343
  ;; 34. seti 0 1 0  -> A = 0
  ;; 35. seti 0 5 3  -> D = 0 => GOTO 1

  )
