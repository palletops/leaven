(ns com.palletops.leaven-test
  #+clj
  (:require
   [com.palletops.leaven :as leaven :refer [start stop defsystem]]
   [com.palletops.leaven.protocols :refer [ILifecycle]]
   [clojure.test :refer [is deftest testing]])
  #+cljs
  (:require-macros
   [cemerick.cljs.test :refer [is deftest testing]]
   [com.palletops.leaven :refer [defsystem]])
  #+cljs
  (:require
   [com.palletops.leaven :as leaven :refer [start stop]]
   [com.palletops.leaven.protocols :as impl :refer [ILifecycle]]
   [cemerick.cljs.test :as t]))

(defrecord TestA [s]
  ILifecycle
  (start [c] c)
  (stop [c] c))

(deftest x
  (let [a (->TestA 1)]
    (is (= a (start a)))
    (is (= a (stop a)))))

(defrecord TestB [start-a stop-a v]
  ILifecycle
  (start [c] (update-in c [:start-a] swap! (fnil conj []) v))
  (stop [c] (update-in c [:stop-a] swap! (fnil conj []) v)))

(defsystem TestSystem [:b1 :b2])

(defn test-system []
  (let [start-a (atom [])
        stop-a (atom [])]
    [start-a stop-a (->TestSystem
                     (->TestB start-a stop-a :b1)
                     (->TestB start-a stop-a :b2))]))

(deftest sequence-test
  (let [[start-a stop-a s] (test-system)
        s1 (start s)]
    (is (= [:b1 :b2] @start-a) "Starts in specified order")
    (is (= [] @stop-a) "Start doesn't stop")
    (let [s2 (stop s1)]
      (is (= [:b1 :b2] @start-a) "Stop propagates state")
      (is (= [:b2 :b1] @stop-a)) "Stops in reverse order")))
