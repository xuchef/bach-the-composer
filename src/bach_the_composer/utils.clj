(ns bach-the-composer.utils
  (:require [clojure.set :as set]))

(defn rename-keys
  [m name-map]
  (into {} (map (fn [[k v]] [(get name-map k k) v]) m)))

(defn same-schema?
  [R S]
  (= (set (:keys R)) (set (:keys S))))

(defn get-common-keys
  [R S]
  (set/intersection (set (:keys R)) (set (:keys S))))

(defn disambiguate
  [R column]
  (keyword (str (:name R) "." (name column))))