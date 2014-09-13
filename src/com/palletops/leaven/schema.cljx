(ns com.palletops.leaven.schema
  "Optional schema support for leaven protocols."
  (:require
   [schema.core :as schema]
   [com.palletops.leaven.protocols :refer [startable? stoppable? queryable?]]))

(def Startable
  (schema/pred startable? "startable?"))

(def Stoppable
  (schema/pred stoppable? "stoppable?"))

(def Queryable
  (schema/pred queryable? "status?"))
