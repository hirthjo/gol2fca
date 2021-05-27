(ns gol2fca.core
  (:require [conexp.io.contexts :refer :all]
            [conexp.fca.contexts :refer :all]
            [conexp.fca.lattices :refer :all]
            [conexp.gui.draw :refer :all]
            [conexp.layouts.dim-draw :refer [dim-draw-layout]]
            [conexp.io.layouts :refer :all]
            [conexp.io.util :refer  :all])
  (:gen-class))

(define-context-output-format :tex
  [ctx file]
  (with-out-writer file
    (println "\\begin{cxt}")
    (println "\\cxtName{}")
    (let [attr (sort (attributes ctx))
          obj (sort (objects ctx))]
      (doseq [a attr]
              (println (str "\\att{" a "}")))
      (doseq [o obj]
              (println (str "\\obj{" 
                            (clojure.string/join "" 
                                                 (for [a attr] 
                                                   (if ((incidence ctx) [o a]) "x" ".")))
                            "}{" o "}")))
      (println "\\end{cxt}"))))

(defn m-idx
  "Index but with modulo. (nth [0 1 2 3] 4) is (nth [0 1 2 3] 0)"
  [idx-bound n]
  (if (< n 0) 
    (+ n idx-bound)
    (mod n idx-bound)))

(defn gol-step
  "Attr and Obj are lists of indices of the GoL grid."
  [obj attr i-fn]
  (let [neighbor-count (fn [i j] 
                         (count 
                          (filter identity 
                                  (for [in [-1 0 1] jn [-1 0 1]
                                        :when (or (not= in 0) (not= jn 0))]
                                    (i-fn [(m-idx (count obj) (+ i in)) 
                                           (m-idx (count attr) (+ j jn))])))))
        gol-incidence (fn [i j] 
                        (let [n-count (neighbor-count i j)
                              alive (i-fn [i j])]
                          (cond
                            (and alive
                                 (contains? #{2 3} n-count)) true
                            (and (not alive)
                                 (= n-count 3)) true
                            :else false)))]
    (make-context obj attr gol-incidence)))

(defn update-drawings
  [ctx i]
  (do 
    (write-context :tex ctx (str "output/context" i ".tex"))
    (write-layout :tikz  
                  (dim-draw-layout (concept-lattice ctx) "genetic" 20 20) 
                  (str "output/lattice_" i ".tikz"))))

(defn -main
  "I don't do a whole lot ... yet."
  [file times]
  (let [odir "output"
        gol-ctx (read-context file :binary-csv)
        obj (sort (objects gol-ctx))
        attr (sort (attributes gol-ctx))]
    (loop [i 0 i-ctx gol-ctx]
      (do (update-drawings i-ctx i)
          (println "done step" i)
          (println i-ctx)
          (if (< i (Integer/parseInt times))
            (recur (inc i)
                   (gol-step obj
                             attr
                             (incidence-relation i-ctx))))))))
