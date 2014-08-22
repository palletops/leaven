(ns com.palletops.leaven.protocols
  "Protocols for leaven components")

(defprotocol ILifecycle
  "Basic lifecycle for a component."
  (start [component]
    "Start a component. Returns an updated component.")
  (stop [component]
    "Stop a component. Returns an updated component."))

(defprotocol IStatus
  "Allows a component to implement a status function, which may just
  have side effects (like logging)."
  (status [component]
    "Allow a component to be queried for status."))
