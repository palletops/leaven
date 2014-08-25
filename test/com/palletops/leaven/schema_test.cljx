(ns com.palletops.leaven.schema-test
  #+clj
  (:require
   [com.palletops.leaven :refer [defsystem lifecycle? status?]]
   [com.palletops.leaven.schema :refer [ILifecycle IStatus]]
   [schema.core :as schema]
   [clojure.test :refer [is deftest testing]])
  #+cljs
  (:require-macros
   [cemerick.cljs.test :refer [is deftest testing]]
   [com.palletops.leaven :refer [defsystem]])
  #+cljs
  (:require
   [com.palletops.leaven :refer [lifecycle? status?]]
   [com.palletops.leaven.schema :refer [ILifecycle IStatus]]
   [cemerick.cljs.test :as t]
   [schema.core :as schema]))

(defsystem A [])

(deftest schema-test
  (is (lifecycle? (->A)))
  (is (status? (->A)))
  (testing "success"
    (is (schema/validate ILifecycle (->A)))
    (is (schema/validate IStatus (->A))))
  (testing "failure"
    (is (thrown? #+cljs js/Error #+clj Exception
                 (schema/validate ILifecycle {})))
    (is (thrown? #+cljs js/Error #+clj Exception
                 (schema/validate IStatus {})))))
