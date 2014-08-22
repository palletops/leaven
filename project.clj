(defproject com.palletops/leaven "0.1.0-SNAPSHOT"
  :description "A lightweight component library for clojure and clojurescript."
  :url "https://github.com/palletops/leaven"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[com.keminglabs/cljx "0.4.0"]
            [com.cemerick/clojurescript.test "0.3.1"]
            [lein-cljsbuild "1.0.3"]]
  :hooks [leiningen.cljsbuild]
  :prep-tasks ["cljx" "javac" "compile"]
  :source-paths ["target/generated/src/clj"]
  :resource-paths ["target/generated/src/cljs"]
  :test-paths ["target/generated/test/clj"]
  :aliases {"auto-test" ["do" "clean," "cljsbuild" "auto" "test"]
            "jar" ["do" "cljx," "jar"]
            "test" ["do" "cljx," "test"]})
