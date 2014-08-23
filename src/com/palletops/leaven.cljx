(ns com.palletops.leaven
  "A component composition library."
  (:require
   [com.palletops.leaven.protocols :as protocols
    :refer [ILifecycle IStatus]]))

(defn start
  "Start a component."
  [component]
  (if (satisfies? ILifecycle component)
    (protocols/start component)
    component))

(defn stop
  "Stop a component."
  [component]
  (if (satisfies? ILifecycle component)
    (protocols/stop component)
    component))

(defn status
  "Ask a component for its status."
  [component]
  (if (satisfies? IStatus component)
    (protocols/status component)))


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
(defmacro defsystem
  "Macro to build a system defrecord out of `components`, a sequence
  of keywords that specify the sub-components.  The record will
  implement ILifecycle and IStatus by calling the protocol methods on
  each of the components.  The `start` method calls the sub-components
  in the specified order.  The `stop` method calls the sub-components
  in the reverse order."
  [record-name components]
  (let [rcomponents (vec (reverse components))]
    `(defrecord ~record-name
         [~@(map (comp symbol name) components)]
       ILifecycle
       (~'start [component#]
         (apply-components start component# ~components "starting"))
       (~'stop [component#]
         (apply-components stop component# ~rcomponents "stopping"))
       IStatus
       (~'status [component#]
         (apply-components status component# ~rcomponents "querying status")))))
