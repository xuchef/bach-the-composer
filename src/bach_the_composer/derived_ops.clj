(ns bach-the-composer.derived-ops
  (:require [clojure.set :as set])
  (:require [bach-the-composer.core-ops :refer [selection cross-product projection rename-columns]])
  (:require [bach-the-composer.utils :refer [disambiguate get-common-keys same-schema?]]))

(defn theta-join
  [R S p?]
  (selection (cross-product R S) p?))

(defn natural-join
  [R S]
  (let [common-keys (get-common-keys R S)
        L (set/difference (set/union (set (:keys R)) (set (:keys S)) (set (map #(disambiguate R %) common-keys))) (set common-keys))]
    (rename-columns
     (projection
      (theta-join R S
                  #(every? (fn [key] (= ((disambiguate R key) %) ((disambiguate S key) %))) common-keys))
      L)
     (zipmap (map #(disambiguate R %) common-keys) common-keys))))

(defn intersection
  [R S]
  (assert (same-schema? R S))
  (natural-join R S))
