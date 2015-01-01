(ns com.palletops.leaven.protocols
  "Protocols for leaven components")

(defprotocol Startable
  "Basic lifecycle for a component."
  (start [component]
    "Start a component. Returns an updated component."))

(defprotocol Stoppable
  (stop [component]
    "Stop a component. Returns an updated component."))

(defprotocol Queryable
  "Allows a component to implement a status function, which may just
  have side effects (like logging)."
  (status [component]
    "Allow a component to be queried for status."))

(defn startable?
  "Predicate for testing whether `x` satisfies the Startable protocol."
  [x]
  (satisfies? Startable x))

(defn stoppable?
  "Predicate for testing whether `x` satisfies the Stoppable protocol."
  [x]
  (satisfies? Stoppable x))

(defn queryable?
  "Predicate for testing whether `x` satisfies the Queryable protocol."
  [x]
  (satisfies? Queryable x))
