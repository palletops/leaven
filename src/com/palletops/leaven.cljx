(ns com.palletops.leaven
  "A component composition library."
  (:require
   [com.palletops.leaven.protocols :as protocols
    :refer [ILifecycle IStatus]]))

(defn start
  "Start a component."
  [component]
  (protocols/start component))

(defn stop
  "Stop a component."
  [component]
  (protocols/stop component))

(defn status
  "Ask a component for its status."
  [component]
  (if (satisfies? IStatus component)
    (protocols/status component)))

#+clj
(defmacro defsystem
  "Macro to build a system defrecord out of `components`, a sequence
  of keywords that specify the sub-components.  The record will
  implement ILifecycle and IStatus by calling the protocol methods
  on each of the components."
  [record-name components]
  (letfn [(start-subcomp [k] `(update-in [~k] protocols/start))
          (stop-subcomp [k] `(update-in [~k] protocols/stop))
          (status-subcomp [k] `(update-in [~k] status))]
    `(defrecord ~record-name
         [~@(map (comp symbol name) components)]
       ILifecycle
       (~'start [component#]
         (-> component# ~@(map start-subcomp components)))
       (~'stop [component#]
         (-> component# ~@(map stop-subcomp (reverse components))))
       IStatus
       (~'status [component#]
         (-> component# ~@(map status-subcomp components))))))
