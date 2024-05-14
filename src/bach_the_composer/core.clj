(ns bach-the-composer.core
  (:gen-class)
  (:require [bach-the-composer.table :as table])
  (:require [bach-the-composer.core-ops :as core-ops])
  (:require [bach-the-composer.derived-ops :as derived-ops]))

(defn- section
  [i name]
  (println (apply str (repeat 50 "-")))
  (println (str i ". " name))
  (println (apply str (repeat 50 "-"))))

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
                  [456 "gov"]})
        a (core-ops/rename-table (core-ops/selection member #(> (:uid %) 200)) "A")
        b (core-ops/rename-table (core-ops/selection member #(< (:uid %) 500)) "B")]

    (println (str "What's up Ronny!\n\n"
                  "I had some free time this weekend so I thought I'd learn some Clojure.\n\n"
                  "I'm taking a databases class this term and in it we were recently introduced to relational algebra.\n"
                  "Basically, it's a formal way of representing database queries, and it forms the foundation of SQL queries.\n"
                  "There were some exercises in the lecture notes for us to practice constructing queries.\n"
                  "However, there was no way to verify if our answers were correct or not—I prefer to learn in a REPL environment :)\n\n"
                  "So, that's what this guy's for: an interactive way to view relational algebra.\n"
                  "I'll walk you through some of the functionality. Feel free to play around with it!\n"))

    (section 1 "Basic Functionality")

    (println "For this demo, we'll be working with three tables: User, Group, and Member.\n")

    (table/print-table user)
    (table/print-table group)
    (table/print-table member)

    (println "As you can see, we can display them nicely with `print-table`.")


    (section 2 "Core Operators")

    (println "Here are the core operators that form the crux of relational algebra.\n")

    (println "Renaming: 𝜌(new-name) R")
    (table/print-table (core-ops/rename-table user "𝜌(Homies) User"))

    (println "Selection: σ(p) R")
    (table/print-table (core-ops/rename-table (core-ops/selection user #(> (:popularity %) 0.5)) "σ(popularity>0.5) User"))

    (println "Projection: π(L) R")
    (table/print-table (core-ops/rename-table (core-ops/projection user  #{:name :age}) "π({name,age}) User"))

    (println "Cross-Product: R × S")
    (println " * Note that duplicate columns in two tables are disambiguated when taking the cross-product.")
    (table/print-table (core-ops/rename-table (core-ops/cross-product user member) "User × Member"))

    (println "For the last two core operators, the table schemas must be the same, so let us introduce two new tables:\n")
    (table/print-table a)
    (table/print-table b)

    (println "Union: R ∪ S")
    (table/print-table (core-ops/rename-table (core-ops/union a b) "A ∪ B"))


    (section 3 "Derived operators")

    (println "There's a number of useful operations that we can get by building on top of the core operators.")

    (println "Intersection: R ∩ S")
    (table/print-table (core-ops/rename-table (core-ops/difference a b) "A ∩ B"))

    (println "Theta-join: R ⋈(p) S")
    (println " * Equivalent to σ(p) (R × S)")
    (table/print-table (core-ops/rename-table (derived-ops/theta-join user member #(< (:User.uid %) (:Member.uid %))) "User ⋈(User.uid<Member.uid) Member"))

    (println "Natural-join: R ⋈ S")
    (println " * Equivalent to R ⋈(R.i==S.i for every commmon column i) S, followed by removing the duplicate column(s)")
    (table/print-table (core-ops/rename-table (derived-ops/natural-join user member) "User ⋈ Member"))

    (section 4 "Your turn!")
    (println (str "Thanks for taking the time to go through all this :)\n"
                  "Feel free to play around withe the functions to create some wacky queries.\n"
                  "I'm writing this message 30 mins before my next databases lecture, so stay tuned for more to come!"))))
