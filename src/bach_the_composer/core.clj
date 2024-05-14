(ns bach-the-composer.core
  (:gen-class)
  (:require [clojure.set :as set]))

(defrecord Table [name keys rows])

(defn calc-widths [{:keys [keys rows]}]
  (map (fn [k]
         (reduce #(max
                   %1 (count (str (k %2))))
                 (count (name k))
                 rows))
       keys))

(defn make-table [name keys rows]
  (assert (apply distinct? keys) "keys must be unique")
  (let [rows (set (map #(zipmap keys %) rows))]
    (->Table name
             keys
             rows)))

(defn print-fixed-width [text width]
  (let [padding (- width (count text))]
    (print text)
    (print (apply str (repeat padding " ")))))

(defn print-table [table]
  (let [column-widths (zipmap (:keys table) (calc-widths table))
        total-width (+ (+ (reduce + (vals column-widths)) (count (:keys table))) 1)
        line (apply str (repeat total-width "-"))]
    (println (:name table))
    (println line)
    (print "|")
    (doseq [k (:keys table)]
      (print-fixed-width (name k) (k column-widths))
      (print "|"))
    (println)
    (println line)
    (doseq [r (:rows table)]
      (print "|")
      (doseq [k (:keys table)]
        (print-fixed-width (str (k r)) (k column-widths))
        (print "|"))
      (println))
    (println line)
    (println)))

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

(defn rename-table [R new-name]
  (assoc R :name new-name))

(defn rename-keys [m name-map]
  (into {} (map (fn [[k v]] [(get name-map k k) v]) m)))

(defn rename-columns
  [R name-map]
  (assoc R
         :keys (map #(% name-map %) (:keys R))
         :rows (set (map #(rename-keys % name-map) (:rows R)))))

(defn rename
  "rename the table and a set of columns"
  [R new-name name-map]
  (rename-columns (rename-table R new-name) name-map))

(defn same-schema? [R S] (= (set (:keys R)) (set (:keys S))))

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

(defn get-common-keys [R S] (set/intersection (set (:keys R)) (set (:keys S))))

(defn disambiguate [R column] (keyword (str (:name R) "." (name column))))

(defn resolve-duplicate-columns [R S]
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

(defn -main
  "Core and derived relational algebra operations"
  [& args]
  (let [user (make-table "User"
                         [:uid :name :age :popularity]
                         #{[142 "Bart" 10 0.9]
                           [123 "Milhouse" 10 0.2]
                           [857 "Lisa" 8 0.7]
                           [456 "Ralph" 8 0.3]})

        group (make-table "Group"
                          [:gid :name]
                          #{["abc" "A Book Club"]
                            ["gov" "Student Government"]
                            ["dps" "Dead Putting Society"]})

        member (make-table "Member"
                           [:uid :gid]
                           #{[142 "dps"]
                             [123 "gov"]
                             [857 "abc"]
                             [857 "gov"]
                             [456 "abc"]
                             [456 "gov"]})]

    (print-table (selection user #(> (:uid %) 125)))
    (print-table (projection user [:age]))
    (print-table (rename user "BROO" {:uid :broskid :age :years}))
    (print-table member)
    (let [a (rename-table (selection member #(> (:uid %) 200)) "A")
          b (rename-table (selection member #(< (:uid %) 500)) "B")]
      (print-table a)
      (print-table b)
      (print-table (rename-table (union a b) "A U B"))
      (print-table (rename-table (difference a b) "A - B"))
      (print-table (rename-table (difference b a) "B - A"))
      (print-table (rename-table (intersection a b) "A ∩ B")))

    (print-table user)
    (print-table group)
    (print-table (rename-table (cross-product user member) "User X Member"))
    (print-table (rename-table (theta-join user member #(= (:User.uid %) (:Member.uid %))) "User ⋈_{User.uid=Member.uid} member"))
    (print-table (rename-table (natural-join user member) "User ⋈ member"))

    ;; (print-table (selection (rename-table (cross-product user member) "User X Member") #(> (:age %) )))
    ;; (println (:key-to-idx user))
    ))
