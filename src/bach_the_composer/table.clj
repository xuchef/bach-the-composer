(ns bach-the-composer.table)

(defrecord Table [name keys rows])

(defn create-table
  [name keys rows]
  (assert (apply distinct? keys) "keys must be unique")
  (let [rows (set (map #(zipmap keys %) rows))]
    (->Table name
             keys
             rows)))

(defn- calc-widths [{:keys [keys rows]}]
  (map (fn [k]
         (reduce #(max
                   %1 (count (str (k %2))))
                 (count (name k))
                 rows))
       keys))

(defn- print-fixed-width
  [text width]
  (let [padding (- width (count text))]
    (print text)
    (print (apply str (repeat padding " ")))))

(defn print-table
  [table]
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