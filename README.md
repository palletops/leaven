# leaven

> __leav·en__  /ˈlevən/
>
> noun  - a pervasive influence that modifies something or transforms it for the better.
>
> verb  - permeate and modify or transform (something) for the better.

A lightweight component model for clojure and clojurescript.

## Install

Add `[com.palletops/leaven "0.1.1"]` to your `:dependencies`.

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
and stopped in the order specified.  The macro defines a record, and
you should provide a function to instantiate the record with the
sub-component instances.

For component providers, `com.palletops.leaven.protocols` provides the
`ILifecycle` protocol, and requires the implementation of the `start`
and `stop` methods.  The `IStatus` protocol provides for a `status`
method.

### Example


### Comparison with Component Example

This is the example from the [Component][Component] readme, translated
for leaven.

```clj
(ns com.example.your-application
  (:require [com.palletops.leaven :as leaven]))

(defrecord Database [host port connection]
  leaven/ILifecycle
  (start [component]
    (let [conn (connect-to-database host port)]
      (assoc component :connection conn)))

  (stop [component]
    (.close connection)
    (assoc component :connection nil)))

(defn database [host port]
  (map->Database {:host host :port port}))

(defrecord ExampleComponent [options cache database]
  leaven/ILifecycle
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
    (map->ExampleSystem
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
