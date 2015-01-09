(ns com.palletops.leaven.component
  "Adapters for Component components"
  (:require
   [com.palletops.leaven :as leaven]
   [com.palletops.leaven.protocols :as protocols]
   [com.stuartsierra.component :as component]))

(defmacro extend-leaven
  "Extend a component type with the leaven protocols."
  [component-class]
  `(extend-type ~component-class
     protocols/Startable
     (~'start [~'component]
       (component/start ~'component))
     protocols/Stoppable
     (~'stop [~'component]
       (component/stop ~'component))))

(defmacro extend-component
  "Extend a leaven type with the component protocol."
  [component-class]
  `(extend-type ~component-class
     component/Lifecycle
     (~'start [~'component] (leaven/start ~'component))
     (~'stop [~'component] (leaven/stop ~'component))))
