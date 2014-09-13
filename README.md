# leaven

> __leav·en__  /ˈlevən/
>
> noun  - a pervasive influence that modifies something or transforms it for the better.
>
> verb  - permeate and modify or transform (something) for the better.

A lightweight component model for clojure and clojurescript.

## Install

Add `[com.palletops/leaven "0.2.0"]` to your `:dependencies`.

## Usage

For users of components, `com.palletops.leaven` provides the `start`
and `stop` functions, and the `defsystem` macro.  There is also a
`status` function that can be used with components that support it.

Both `start` and `stop` take a single component as an argument and
return an updated component - components are normally immutable, so
you should always use the updated component in the return value.

The `defsystem` macro is used to define a composite component, made up
of a map of components, each identified by a keyword.  The
sub-components are specified as a vector of keywords, and are started
in the order specified and stop in the reverse order.  The macro
defines a record, and you instantiate the record with the
sub-component instances.

You can specify a body in `defsystem`, just as you would to
`defrecord`, in order to implement other protocols on your system.

For component providers, `com.palletops.leaven.protocols` provides the
`Startable` and `Stoppable` protocols, that require the implementation
of the `start` and `stop` methods respectively.  The `Queryable`
protocol provides for a `status` method.

### Example

We define a component that will provide an increasing sequence of
numbers via a `core.async` channel.  We implement the `Startable` and
`Stoppable` protocols for the component.

```clj
(require '[clojure.core.async :as async]
         '[com.palletops.leaven :as leaven]
         '[com.palletops.leaven.protocols :refer [Startable Stoppable])

(defrecord Counter [init-val channel loop-chan]
  Startable
  (start [component]
    (assoc component :loop-chan
           (async/go-loop [n init-val]
             (async/>! channel n)
             (recur (inc n)))))
  Stoppable
  (stop [component]
    (async/close! channel)
    (assoc component :loop-chan nil)))
```

Note that the record contains fields for both the configuration and
the runtime state of the component.

We instantiate the component with one of the record's constructor
functions.  In this example we use a var to hold the component, but
this is in no way required.

```clj
(def c (async/chan))
(def counter (map->Counter {:init-val 1 :channel c}))
```

We can start the component:

```clj
(alter-var-root #'counter leaven/start)
```

Now we can get values from the channel:

```clj
(async/<!! c) ; => 1
(async/<!! c) ; => 2
(async/<!! c) ; => 3
```

If we had tried to read the channel before starting the component, the
call to `async/<!!` would have blocked.

Stopping the channel:

```clj
(alter-var-root #'counter leaven/stop)
```

And now the channel is closed, so will return nil.

```clj
(async/<!! c) ; => 4
(async/<!! c) ; => nil
```

We're going to define another component now that, take a channel and
will double what is put into it.


```clj
(defrecord Doubler [in-chan out-chan ctrl-chan loop-chan]
  Startable
  (start [component]
    (let [ctrl-chan (async/chan)]
      (assoc component
        :loop-chan (async/go
                     (loop []
                       (let [[v _] (async/alts! [in-chan ctrl-chan])]
                         (if (not= ::stop v)
                           (let [[v _] (async/alts!
                                        [[out-chan (* 2 v)] ctrl-chan])]
                             (if (not= ::stop v)
                               (recur))))))
                     (async/close! out-chan))
        :ctrl-chan ctrl-chan)))
  Stoppable
  (stop [component]
    (async/>!! ctrl-chan ::stop)
    (assoc component :loop-chan nil :ctrl-chan nil)))
```

We'll use these components to define a system

```clj
(leaven/defsystem Evens [:counter :doubler])
(defn evens [out-chan]
  (let [c1 (async/chan)]
    (Evens.
      (map->Counter {:init-val 1 :channel c1})
      (map->Doubler {:in-chan c :out-chan out-chan}))))

(def c (async/chan))
(def sys (evens c))
(alter-var-root #'sys leaven/start)
(async/<!! c) ; => 2
(async/<!! c) ; => 4
(alter-var-root #'sys leaven/stop)
(async/<!! c) ; => nil
```

### Comparison with Component Example

This is the example from the [Component][Component] readme, translated
for leaven.

```clj
(ns com.example.your-application
  (:require [com.palletops.leaven :as leaven]))

(defrecord Database [host port connection]
  leaven/Startable
  (start [component]
    (let [conn (connect-to-database host port)]
      (assoc component :connection conn)))

  (stop [component]
    (.close connection)
    (assoc component :connection nil)))

(defn database [host port]
  (map->Database {:host host :port port}))

(defrecord ExampleComponent [options cache database]
  leaven/Stoppable
  (start [this]
    (assoc this :admin (get-user database "admin")))

  (stop [this]
    this))

(defn example-component [{:keys [config-options db]}]
  (map->ExampleComponent {:db db
                          :options config-options
                          :cache (atom {})}))

(defsystem ExampleSystem [:db :app])

(defn example-system [config-options]
  (let [{:keys [host port]} config-options
        db (database host port)]
    (map->ExampleSystem                 ; normal record constructor
      :db db
      :app (example-component
             {:config-options config-options
              :db db}))))
```

## Library authors

Library authors are encouraged to provide a leaven component in a
separate namespace.  By making the dependency on leaven have a
`provided` scope, you do not force the dependency on your users.

In [leiningen][leiningen], you can make a dependency have `provided`
scop by adding it under the `:provided` profile.

## Why another component library?

The [Component][Component] framework pioneered a component model for
clojure, and provides an excellent rationale for components.

We wanted something that:
- did explicit dependency order, with no need for a `using` function,
- would work in clojurescript

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[Component]:https://github.com/stuartsierra/component "Stuart Sierra's Component"
[leiningen]:https://github.com/technomancy/leiningen "Leiningen"
