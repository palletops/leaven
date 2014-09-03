(ns com.palletops.leaven.readme-test
  (:require
   [com.palletops.leaven :as leaven]
   [com.palletops.leaven.protocols :refer [ILifecycle]]
   [clojure.core.async :as async]
   [clojure.test :refer :all]))

;; We define a component that will provide an increasing sequence of
;; numbers via a `core.async` channel.  We implement the `ILifecycle`
;; protocol for the component.

(defrecord Counter [init-val channel loop-chan]
  ILifecycle
  (start [component]
    (assoc component :loop-chan
           (async/go-loop [n init-val]
             (async/>! channel n)
             (recur (inc n)))))
  (stop [component]
    (async/close! channel)
    (assoc component :loop-chan nil)))

;; Note that the record contains fields for both the configuration and
;; the runtime state of the component.

;; We instantiate the component with one of the record's constructor
;; functions.  In this example we use a var to hold the component, but
;; this is in no way required.

(def c (async/chan))
(def counter (map->Counter {:init-val 1 :channel c}))

;; We can start the component:

(deftest counter-test
  (alter-var-root #'counter leaven/start)
  (is (:loop-chan counter))
  (is (= 1 (async/<!! c)))
  (is (= 2 (async/<!! c)))
  (is (= 3 (async/<!! c)))
  (alter-var-root #'counter leaven/stop)
  (is (= 4 (async/<!! c)))
  (is (nil? (async/<!! c))))

(defrecord Doubler [in-chan out-chan ctrl-chan loop-chan]
  ILifecycle
  (start [component]
    (let [ctrl-chan (async/chan)]
      (assoc component
        :loop-chan (async/go
                     (loop []
                       (let [[v _] (async/alts! [in-chan ctrl-chan])]
                         (if (not= ::stop v)
                           (let [[v _] (async/alts!
                                        [[out-chan (* 2 v)] ctrl-chan])]
                             (if (not= ::stop v)
                               (recur))))))
                     (async/close! out-chan))
        :ctrl-chan ctrl-chan)))
  (stop [component]
    (async/>!! ctrl-chan ::stop)
    (assoc component :loop-chan nil :ctrl-chan nil)))

(leaven/defsystem Evens [:counter :doubler])

(defn evens [out-chan]
  (let [c1 (async/chan)]
    (Evens.
      (map->Counter {:init-val 1 :channel c1})
      (map->Doubler {:in-chan c1 :out-chan out-chan}))))

(def c2 (async/chan))
(def sys (evens c2))

(deftest evens-test
  (alter-var-root #'sys leaven/start)
  (is (= 2 (async/<!! c2)))
  (is (= 4 (async/<!! c2)))
  (alter-var-root #'sys leaven/stop)
  (is (nil? (async/<!! c2))))
