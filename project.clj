(defproject com.palletops/leaven "0.1.2-SNAPSHOT"
  :description "A lightweight component library for clojure and clojurescript."
  :url "https://github.com/palletops/leaven"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.palletops/api-builder "0.3.0"]]
  :hooks [cljx.hooks leiningen.cljsbuild]
  :prep-tasks ["javac" "compile"]
  :source-paths ["target/generated/src/clj"]
  :resource-paths ["target/generated/src/cljs"]
  :test-paths ["target/generated/test/clj"]
  :aliases {"auto-test" ["do" "clean," "cljsbuild" "auto" "test"]}
  :cljsbuild {:builds []}
  :cljx {:builds [{:source-paths ["src"]
                   :output-path "target/generated/src/clj"
                   :rules :clj}
                  {:source-paths ["src"]
                   :output-path "target/generated/src/cljs"
                   :rules :cljs}]})
