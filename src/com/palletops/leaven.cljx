(ns com.palletops.leaven
  "A component composition library."
  #+clj
  (:require
   [com.palletops.leaven.protocols :as protocols]
   [com.palletops.api-builder.api :refer [defn-api]]
   [schema.core :as schema])
  #+cljs
  (:require-macros
   [com.palletops.api-builder.api :refer [defn-api]])
  #+cljs
  (:require
   [com.palletops.leaven.protocols :as protocols]
   [schema.core :as schema]))

(defn-api start
  "Start a component."
  {:sig [[schema/Any :- schema/Any]]}
  [component]
  (if (protocols/lifecycle? component)
    (protocols/start component)
    component))

(defn-api stop
  "Stop a component."
  {:sig [[schema/Any :- schema/Any]]}
  [component]
  (if (protocols/lifecycle? component)
    (protocols/stop component)
    component))

(defn-api status
  "Ask a component for its status."
  {:sig [[schema/Any :- schema/Any]]}
  [component]
  (if (protocols/status? component)
    (protocols/status component)))

(defn-api lifecycle?
  "Predicate for testing whether `x` satisfies the ILifecycle protocol."
  {:sig [[schema/Any :- schema/Any]]}
  [x]
  (protocols/lifecycle? x))

(defn-api status?
  "Predicate for testing whether `x` satisfies the IStatus protocol."
  {:sig [[schema/Any :- schema/Any]]}
  [x]
  (protocols/status? x))

(defn ^:internal apply-components
  "Execute a function on a sequence of components from a record.
  Exceptions are caught and propagate with a `:system` data element
  that contains the partially updated system component, a `:component`
  key that is the keyword for the component that caused the exception,
  and `:incomplete` which is a sequence of keywords for the components
  where the operation was not completed."
  [f rec sub-components operation-name]
  (loop [rec rec cs sub-components]
    (if-let [c (first cs)]
      (let [res (try
                  (update-in rec [c] f)
                  (catch #+cljs js/Error #+clj Exception e
                         (throw
                          (ex-info
                           (str "Exception while " operation-name
                                " " c
                                " system sub-component.")
                           {:system rec
                            :component c
                            :sub-components sub-components
                            :completed (subvec sub-components
                                               0 (- (count sub-components)
                                                    (count cs)))
                            :uncompleted cs}
                           e))))]
        (recur res (rest cs)))
      rec)))

#+clj
(defmacro ^:api defsystem
  "Macro to build a system defrecord out of `components`, a sequence
  of keywords that specify the sub-components.  The record will
  implement ILifecycle and IStatus by calling the protocol methods on
  each of the components.  The `start` method calls the sub-components
  in the specified order.  The `stop` method calls the sub-components
  in the reverse order.

  A body can be supplied as used by defrecord, to implement extra
  protocols on the system."
  [record-name components & body]
  (let [rcomponents (vec (reverse components))]
    `(defrecord ~record-name
         [~@(map (comp symbol name) components)]
       ~@body
       protocols/ILifecycle
       (~'start [component#]
         (apply-components start component# ~components "starting"))
       (~'stop [component#]
         (apply-components stop component# ~rcomponents "stopping"))
       protocols/IStatus
       (~'status [component#]
         (apply-components status component# ~rcomponents "querying status")))))
