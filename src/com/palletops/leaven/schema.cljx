(ns com.palletops.leaven.schema
  "Optional schema support for leaven protocols."
  (:require
   [schema.core :as schema]
   [com.palletops.leaven.protocols :refer [lifecycle? status?]]))

(def ILifecycle
  (schema/pred lifecycle? "lifecycle?"))

(def IStatus
  (schema/pred status? "status?"))
