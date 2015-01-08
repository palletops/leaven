(ns com.palletops.leaven-test
  #+clj
  (:require
   [com.palletops.leaven :as leaven
    :refer [start stop defsystem startable? stoppable? update-components]]
   [com.palletops.leaven.protocols :refer [Startable Stoppable]]
   [clojure.test :refer [is deftest testing]])
  #+cljs
  (:require-macros
   [cemerick.cljs.test :refer [is deftest testing]]
   [com.palletops.leaven :refer [defsystem]])
  #+cljs
  (:require
   [com.palletops.leaven :as leaven
    :refer [start stop startable? stoppable? update-components]]
   [com.palletops.leaven.protocols :as impl :refer [Startable Stoppable]]
   [cemerick.cljs.test :as t]))

(defrecord TestA [s]
  Startable
  (start [c] c)
  Stoppable
  (stop [c] c))

(deftest x
  (let [a (->TestA 1)]
    (is (= a (start a)))
    (is (= a (stop a)))))

(defrecord TestB [start-a stop-a v]
  Startable
  (start [c] (update-in c [:start-a] swap! (fnil conj []) v))
  Stoppable
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
  Startable
  (start [c] (throw (ex-info "start-failed" {})))
  Stoppable
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
(defsystem TestExtend [b1 b2]
  P
  (p [_] :p))

(deftest defsystem-extend-test
  (is (= :p (p (TestExtend. nil nil)))))


(defrecord TestComp [v]
  Startable
  (start [component]
    (assoc component :v ::v)))

(defsystem SystemOptionsTest [:b1 :b2]
  {:on-start {:b1 (update-components [:b2])}
   :on-stop {:b1 #(assoc-in %1 [%2 :v] nil)}})

(deftest defsystem-options-test
  (let [c (TestComp. nil)
        s (map->SystemOptionsTest {:b1 c
                                   :b2 {:b1 c}})]
    (is (nil? (get-in s [:b2 :b1 :v])))
    (let [ss (start s)]
      (is (= ::v (get-in ss [:b2 :b1 :v])))
      (let [st (stop ss)]
        (is (nil? (get-in st [:b1 :v])))
        (is (= ::v (get-in ss [:b2 :b1 :v])))))))

(defsystem TestSystemOptions2 [:b1 :b2]
  {:on-start {:b1 (update-components {:b2 :b11})}})

(deftest defsystem-options-on-start-map-test
  (let [c (TestComp. nil)
        s (map->TestSystemOptions2 {:b1 c
                                    :b2 {:b11 c}})
        ss (start s)]
    (is (= ::v (get-in ss [:b2 :b11 :v])))))

(defsystem TestSystemOptions3 [:b1 :b2]
  {:depends {:b2 [:b1]}})

(deftest defsystem-options-depends-vector-test
  (let [c (TestComp. nil)
        s (map->TestSystemOptions3 {:b1 c
                                    :b2 {:b1 c}})
        ss (start s)]
    (is (= ::v (get-in ss [:b2 :b1 :v])))))

(defsystem TestSystemOptions4 [:b1 :b2]
  {:depends {:b2 {:b1 :b11}}})

(deftest defsystem-options-depends-map-test
  (let [c (TestComp. nil)
        s (map->TestSystemOptions4 {:b1 c
                                    :b2 {:b11 c}})
        ss (start s)]
    (is (= ::v (get-in ss [:b2 :b11 :v])))))
