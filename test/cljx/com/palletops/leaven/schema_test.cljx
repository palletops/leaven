(ns com.palletops.leaven.schema-test
  #+clj
  (:require
   [com.palletops.leaven :refer [defsystem startable? stoppable? queryable?]]
   [com.palletops.leaven.schema :refer [Startable Stoppable Queryable]]
   [schema.core :as schema]
   [clojure.test :refer [is deftest testing]])
  #+cljs
  (:require-macros
   [cemerick.cljs.test :refer [is deftest testing]]
   [com.palletops.leaven :refer [defsystem]])
  #+cljs
  (:require
   [com.palletops.leaven :refer [startable? stoppable? queryable?]]
   [com.palletops.leaven.schema :refer [Startable Stoppable Queryable]]
   [cemerick.cljs.test :as t]
   [schema.core :as schema]))

(defsystem A [])

(deftest schema-test
  (is (startable? (->A)))
  (is (stoppable? (->A)))
  (is (queryable? (->A)))
  (testing "success"
    (is (schema/validate Startable (->A)))
    (is (schema/validate Stoppable (->A)))
    (is (schema/validate Queryable (->A))))
  (testing "failure"
    (is (thrown? #+cljs js/Error #+clj Exception
                 (schema/validate Startable {})))
    (is (thrown? #+cljs js/Error #+clj Exception
                 (schema/validate Stoppable {})))
    (is (thrown? #+cljs js/Error #+clj Exception
                 (schema/validate Queryable {})))))
