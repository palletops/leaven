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

(defrecord TestThrow []
  ILifecycle
  (start [c] (throw (ex-info "start-failed" {})))
  (stop [c] (throw (ex-info "stop-failed" {}))))

(defn test-throw-system []
  (->TestSystem
   (->TestA :a)
   (->TestThrow)))

(deftest exception-test
  (let [s (test-throw-system)]
    (is (thrown-with-msg? #+cljs js/Error #+clj Exception
                          #"Exception while starting.*"
                 (start s)))
    (is (thrown-with-msg? #+cljs js/Error #+clj Exception
                 #"Exception while stopping.*"
                 (stop s)))
    (try
      (start s)
      (catch #+cljs js/Error #+clj Exception e
             (let [{:keys [component system sub-components completed]}
                   (ex-data e)]
               (is (= :b2 component) "Reports the failed component"))))
    (try
      (stop s)
      (catch #+cljs js/Error #+clj Exception e
             (let [{:keys [component system sub-components completed]}
                   (ex-data e)]
               (is (= :b2 component) "Reports the failed component"))))))

(defprotocol P (p [_] "return a :p"))
(defsystem TestExtend [:b1 :b2]
  P
  (p [_] :p))

(deftest defsystem-extend-test
  (is (= :p (p (TestExtend. nil nil)))))
