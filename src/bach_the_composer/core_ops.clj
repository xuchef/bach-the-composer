(ns bach-the-composer.core-ops
  (:require [clojure.set :as set])
  (:require [bach-the-composer.utils :refer [rename-keys same-schema? get-common-keys disambiguate]]))

(defn selection
  "same columns as R, but only rows of R that satisfy a predicate p"
  [R p?]
  (assoc R :rows (set (filter p? (:rows R)))))

(defn projection
  "same rows as R, but only the columns in L"
  [R L]
  (assert (set/subset? (set L) (set (:keys R))))
  (assoc R
         :keys L
         :rows (set (map #(select-keys % L) (:rows R)))))

(defn rename-table
  [R new-name]
  (assoc R :name new-name))

(defn rename-columns
  [R name-map]
  (assoc R
         :keys (map #(% name-map %) (:keys R))
         :rows (set (map #(rename-keys % name-map) (:rows R)))))

(defn rename
  "rename the table and a set of columns"
  [R new-name name-map]
  (rename-columns (rename-table R new-name) name-map))

(defn union
  "contains all rows in R and all rows in S (with duplicate rows removed)"
  [R S]
  (assert (same-schema? R S))
  (assoc R :rows (set/union (:rows R) (:rows S))))

(defn difference
  "contains all rows in R that are not in S"
  [R S]
  (assert (same-schema? R S))
  (assoc R :rows (set/difference (:rows R) (:rows S))))


(defn- resolve-duplicate-columns
  [R S]
  (assert (not= (:name R) (:name S)))
  (let [common-keys (get-common-keys R S)]
    (if (empty? common-keys)
      [R S]
      [(rename-columns R (zipmap common-keys (map #(disambiguate R %) common-keys)))
       (rename-columns S (zipmap common-keys (map #(disambiguate S %) common-keys)))])))

(defn cross-product
  "for each row r in R and each s in S, output a row rs"
  [R S]
  (let [[R S] (resolve-duplicate-columns R S)]
    (assoc R
           :keys (concat (:keys R) (:keys S))
           :rows (set (reduce (fn [acc i] (into acc (map #(conj % i) (:rows S))))
                              []
                              (:rows R))))))
