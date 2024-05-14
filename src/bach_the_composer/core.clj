(ns bach-the-composer.core
  (:gen-class)
  (:require [bach-the-composer.table :as table])
  (:require [bach-the-composer.core-ops :as core-ops])
  (:require [bach-the-composer.derived-ops :as derived-ops]))

(defn -main
  "Core and derived relational algebra operations"
  [& args]
  (let [user (table/create-table
              "User"
              [:uid :name :age :popularity]
              #{[142 "Bart" 10 0.9]
                [123 "Milhouse" 10 0.2]
                [857 "Lisa" 8 0.7]
                [456 "Ralph" 8 0.3]})
        group (table/create-table
               "Group"
               [:gid :name]
               #{["abc" "A Book Club"]
                 ["gov" "Student Government"]
                 ["dps" "Dead Putting Society"]})
        member (table/create-table
                "Member"
                [:uid :gid]
                #{[142 "dps"]
                  [123 "gov"]
                  [857 "abc"]
                  [857 "gov"]
                  [456 "abc"]
                  [456 "gov"]})]

    (table/print-table (core-ops/selection user #(> (:uid %) 125)))
    (table/print-table (core-ops/projection user [:age]))
    (table/print-table (core-ops/rename user "BROO" {:uid :broskid :age :years}))
    (table/print-table member)
    (let [a (core-ops/rename-table (core-ops/selection member #(> (:uid %) 200)) "A")
          b (core-ops/rename-table (core-ops/selection member #(< (:uid %) 500)) "B")]
      (table/print-table a)
      (table/print-table b)
      (table/print-table (core-ops/rename-table (core-ops/union a b) "A U B"))
      (table/print-table (core-ops/rename-table (core-ops/difference a b) "A - B"))
      (table/print-table (core-ops/rename-table (core-ops/difference b a) "B - A"))
      (table/print-table (core-ops/rename-table (derived-ops/intersection a b) "A ∩ B")))

    (table/print-table user)
    (table/print-table group)
    (table/print-table (core-ops/rename-table (core-ops/cross-product user member) "User X Member"))
    (table/print-table (core-ops/rename-table (derived-ops/theta-join user member #(= (:User.uid %) (:Member.uid %))) "User ⋈_{User.uid=Member.uid} member"))
    (table/print-table (core-ops/rename-table (derived-ops/natural-join user member) "User ⋈ member"))

    ;; (table/print (selection (rename-table (cross-product user member) "User X Member") #(> (:age %) )))
    ;; (println (:key-to-idx user))
    ))
