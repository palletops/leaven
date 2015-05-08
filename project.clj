(defproject com.palletops/leaven "0.3.1"
  :description "A lightweight component library for clojure and clojurescript."
  :url "https://github.com/palletops/leaven"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.palletops/api-builder "0.3.1"]]
  :plugins [[com.keminglabs/cljx "0.5.0"]]
  :prep-tasks [["cljx" "once"]]
  :source-paths ["src/clj" "target/generated/src/clj"]
  :resource-paths ["target/generated/src/cljs"]
  :test-paths ["target/generated/test/clj"]
  :aliases {"compile" ["do" "compile," "cljsbuild" "once"]
            "test" ["do" "test," "cljsbuild" "once" "test," "cljsbuild" "test"]
            "auto-test" ["do" "clean," "cljsbuild" "auto" "test"]}
  :cljsbuild {:builds []}
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/generated/src/clj"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/generated/src/cljs"
                   :rules :cljs}]})
