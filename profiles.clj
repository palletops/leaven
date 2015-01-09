{:provided {:dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/clojurescript "0.0-2277"]
                           [prismatic/schema "0.2.6"]
                           [com.stuartsierra/component "0.2.2"]]}

 :cljs-test {:cljx
             {:builds
              ;; Using :replace and repeating the src paths, as
              ;; otherwise test paths get generated multiple times.
              ^:replace [{:source-paths ["src/cljx"]
                          :output-path "target/generated/src/clj"
                          :rules :clj}
                         {:source-paths ["src/cljx"]
                          :output-path "target/generated/src/cljs"
                          :rules :cljs}
                         {:source-paths ["test/cljx"]
                          :output-path "target/generated/test/clj"
                          :rules :clj}
                         {:source-paths ["test/cljx"]
                          :output-path "target/generated/test/cljs"
                          :rules :cljs}]}
             :cljsbuild
             ^:replace
             {:test-commands
              {"tests"      ["phantomjs" "runners/runner-none.js"
                             "target/unit-test" "target/unit-test.js"]
               ;; "node-tests" ["node" "runners/runner-none-node.js"
               ;;               "target/unit-test-node"
               ;;               "target/unit-test-node.js"]
               }
              :builds
              [{:id "test"
                :source-paths ["target/generated/src/clj"
                               "target/generated/src/cljs"
                               "target/generated/test/cljs"]
                :compiler {:output-to "target/unit-test.js"
                           :output-dir "target/unit-test"
                           :source-map "target/unit-test.js.map"
                           :optimizations :none
                           :pretty-print true}}
               ;; {:id "test-node"
               ;;   :source-paths ["target/generated/src/clj"
               ;;                  "target/generated/src/cljs"
               ;;                  "target/generated/test/cljs"]
               ;;   :compiler {:output-to "target/unit-test-node.js"
               ;;              :target :nodejs
               ;;              :output-dir "target/unit-test-node"
               ;;              :optimizations :none
               ;;              :pretty-print true}}
               ]}
             }
 :clj-test {:test-paths ["test/clj" "target/generated/test/clj"]
            :dependencies [[org.clojure/core.async "0.1.346.0-17112a-alpha"]]}
 :cljx {:dependencies [[com.keminglabs/cljx "0.5.0"]]
        ;; plugin doesn't seem to add this now?
        :repl-options {:nrepl-middleware [cljx.repl-middleware/wrap-cljx]}}
 :dev-base {:plugins [[lein-pallet-release "RELEASE"]
                      [com.cemerick/austin "0.1.5"]
                      [com.cemerick/clojurescript.test "0.3.3"]
                      [lein-cljsbuild "1.0.3"]]}
 :dev [:dev-base :cljx :cljs-test :clj-test]
 :test [:cljs-test :clj-test]}
