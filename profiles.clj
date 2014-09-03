{:provided {:dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/clojurescript "0.0-2277"]
                           [prismatic/schema "0.2.6"]
                           [com.keminglabs/cljx "0.4.0"]]}

 :dev {:plugins [[lein-pallet-release "RELEASE"]
                 [com.cemerick/austin "0.1.5"]
                 [com.cemerick/clojurescript.test "0.3.1"]
                 [lein-cljsbuild "1.0.3"]
                 [com.keminglabs/cljx "0.4.0"]]
       :dependencies [[org.clojure/core.async "0.1.338.0-5c5012-alpha"]]
       :cljx {:builds [{:source-paths ["test"]
                        :output-path "target/generated/test/clj"
                        :rules :clj}
                       {:source-paths ["test"]
                        :output-path "target/generated/test/cljs"
                        :rules :cljs}]}
       :cljsbuild {:test-commands
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
                    ]}}}
