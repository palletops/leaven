(ns com.palletops.leaven
  "A component composition library."
  #+clj
  (:require
   [com.palletops.leaven.protocols :as protocols]
   [com.palletops.api-builder.api :refer [defn-api]]
   [schema.core :as schema :refer [=>]])
  #+cljs
  (:require-macros
   [com.palletops.api-builder.api :refer [defn-api]]
   [schema.macros :refer [=>]])
  #+cljs
  (:require
   [com.palletops.leaven.protocols :as protocols]
   [schema.core :as schema]))

(defn-api start
  "Start a component."
  {:sig [[schema/Any :- schema/Any]]}
  [component]
  (if (protocols/startable? component)
    (protocols/start component)
    component))

(defn-api stop
  "Stop a component."
  {:sig [[schema/Any :- schema/Any]]}
  [component]
  (if (protocols/stoppable? component)
    (protocols/stop component)
    component))

(defn-api status
  "Ask a component for its status."
  {:sig [[schema/Any :- schema/Any]]}
  [component]
  (if (protocols/queryable? component)
    (protocols/status component)))

(defn-api startable?
  "Predicate for testing whether `x` satisfies the Startable protocol."
  {:sig [[schema/Any :- schema/Any]]}
  [x]
  (protocols/startable? x))

(defn-api stoppable?
  "Predicate for testing whether `x` satisfies the Stoppable protocol."
  {:sig [[schema/Any :- schema/Any]]}
  [x]
  (protocols/stoppable? x))

(defn-api queryable?
  "Predicate for testing whether `x` satisfies the Queryable protocol."
  {:sig [[schema/Any :- schema/Any]]}
  [x]
  (protocols/queryable? x))

(defn ^:internal apply-components
  "Execute a function on a sequence of components from a record.
  Exceptions are caught and propagate with a `:system` data element
  that contains the partially updated system component, a `:component`
  key that is the keyword for the component that caused the exception,
  and `:incomplete` which is a sequence of keywords for the components
  where the operation was not completed."
  [f rec sub-components operation-name on-f]
  (loop [rec rec cs sub-components]
    (if-let [c (first cs)]
      (let [post-f (get on-f c)
            res (try
                  (-> (update-in rec [c] f)
                      (cond-> post-f (post-f c)))
                  (catch #+cljs js/Error #+clj Throwable e
                         (let [d (ex-data e)]
                           (throw
                            (ex-info
                             (str "Exception while " operation-name
                                  " " c
                                  " system sub-component.")
                             (merge
                              {:system rec
                               :component c
                               :sub-components sub-components
                               :completed (subvec sub-components
                                                  0 (- (count sub-components)
                                                       (count cs)))
                               :uncompleted cs
                               :operation-name operation-name
                               :type ::system-failed}
                              (if (= ::system-failed (:type d))
                                ;; ensure we report the most nested details
                                d))
                             e)))))]
        (recur res (rest cs)))
      rec)))

(defn-api update-components
  "Returns a function to update the system components given by
  `component-specs`.


  , with a sub-component, assuming the component acts like a map.

  Can be used as a value in an :on-start option map in defrecord to
  get components updated with their started values."
  {:sig [[(schema/either
           [schema/Keyword]
           {schema/Keyword schema/Keyword})
          :- (=> schema/Any schema/Any schema/Keyword)]]}
  [component-specs]
  (fn [component sub-kw]
    (reduce
     #(cond
       (keyword? %2) (assoc-in %1 [%2 sub-kw] (sub-kw component))
       :else (assoc-in %1 [(key %2) (val %2)] (sub-kw component)))
     component
     component-specs)))

(defn comp-on-f
  "Compose two functions for use with an on-start or on-finish
  function."
  [f1 f2]
  (fn [c kw]
    (-> c
        (f1 :channel)
        (f2 :channel))))

#+clj
(defmacro ^:api defsystem
  "Macro to build a system defrecord out of `components`, a sequence
  of keywords that specify the sub-components.  The record will
  implement Startable, Stoppable and Queryable by calling the protocol
  methods on each of the components.  The `start` method calls the
  sub-components in the specified order.  The `stop` method calls the
  sub-components in the reverse order.

  An option map may be supplied after the sub-component vector.  This
  can be used to pass functions that are called after the
  sub-component is operated on. Each function must take component, and
  sub-component keyword, and return a possibly modified component map.
  Values can be specified for :on-start and :on-stop.

  {:on-start
    {:sub-comp1 (update-subcomponent :sub-comp2)
     :sub-comp2 #(assoc-in %1 [:sub-comp2 :comp1] %3)}}

  A body can be supplied as used by defrecord, to implement extra
  protocols on the system."
  [record-name components & body]
  (let [component-syms (mapv (comp symbol name) components)
        component-kws (mapv (comp keyword name) components)
        rcomponents (vec (reverse component-kws))
        options (let [opts (first body)]
                  (if (map? opts) opts))
        body (if options
               (rest body)
               body)
        option-sym (gensym "options")]  ; auto gensym always gives same value
    `(do
       (def ~option-sym ~options) ; defrecord functions can't access lexical scope
       (defrecord ~record-name [~@component-syms]
         ~@body
         protocols/Startable
         (~'start [component#]
           (apply-components
            start component# ~component-kws "starting" (:on-start ~option-sym)))
         protocols/Stoppable
         (~'stop [component#]
           (apply-components
            stop component# ~rcomponents "stopping" (:on-stop ~option-sym)))
         protocols/Queryable
         (~'status [component#]
           (apply-components
            status component# ~rcomponents "querying status" nil))))))
