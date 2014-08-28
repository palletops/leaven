(defproject com.palletops/leaven "0.1.1-SNAPSHOT"
  :description "A lightweight component library for clojure and clojurescript."
  :url "https://github.com/palletops/leaven"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.palletops/api-builder "0.3.0"]]
  :hooks [leiningen.cljsbuild]
  :prep-tasks ["cljx" "javac" "compile"]
  :source-paths ["target/generated/src/clj"]
  :resource-paths ["target/generated/src/cljs"]
  :test-paths ["target/generated/test/clj"]
  :aliases {"auto-test" ["do" "clean," "cljx," "cljsbuild" "auto" "test"]
            "jar" ["do" "cljx," "jar"]
            "install" ["do" "cljx," "install"]
            "test" ["do" "cljx," "test"]}
  :cljsbuild {:builds []})
