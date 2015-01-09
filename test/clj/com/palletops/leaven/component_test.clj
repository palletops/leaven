(ns com.palletops.leaven.component-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.leaven :as leaven]
   [com.palletops.leaven.component :refer :all]
   [com.palletops.leaven.protocols :as protocols]
   [com.stuartsierra.component :as component]))

(defrecord C [state]
  component/Lifecycle
  (start [component]
    (assoc component :state :started))
  (stop [component]
    (assoc component :state :stopped)))

(extend-leaven C)

(deftest component->leaven-test
  (let [c (C. :c)]
    (is (= :c (:state c)))
    (is (= :started (:state (leaven/start c))))
    (is (= :stopped (:state (leaven/stop c))))))

(defrecord L [state]
  protocols/Startable
  (start [component]
    (assoc component :state :started))
  protocols/Stoppable
  (stop [component]
    (assoc component :state :stopped)))

(extend-component L)

(deftest leaven->component-test
  (let [l (L. :l)]
    (is (= :l (:state l)))
    (is (= :started (:state (component/start l))))
    (is (= :stopped (:state (component/stop l))))))
