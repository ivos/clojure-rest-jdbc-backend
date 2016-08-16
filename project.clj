(defproject clojure-rest-jdbc-backend "0.1.0-SNAPSHOT"
  :description "A REST backend on JDBC written in Clojure."
  :url "https://github.com/ivos/clojure-rest-jdbc-backend"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.h2database/h2 "1.3.176"]
                 [com.layerware/hugsql "0.4.7"]
                 [com.zaxxer/HikariCP "2.4.7"]
                 [org.flywaydb/flyway-core "4.0.3"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [compojure "1.5.1"]
                 [slingshot "0.12.2"]
                 [clj-time "0.12.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 ]
  :main ^:skip-aot backend.main
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies   [[reloaded.repl "0.2.2"]
                                        [org.clojure/tools.namespace "0.2.11"]
                                        [org.clojure/tools.nrepl "0.2.12"]
                                        [midje "1.8.3"]
                                        [ring/ring-mock "0.3.0"]
                                        [net.sf.lightair/light-air "3.0.0-SNAPSHOT"]
                                        ]
                       :source-paths   ["dev/src"]
                       :resource-paths ["dev/resources"]
                       :repl-options   {:init-ns user}}
             }
  :plugins [[lein-ring "0.9.7"]
            [lein-uberwar "0.2.0"]]
  :ring {:init    backend.app/start
         :handler backend.app/repl-handler
         :destroy backend.app/stop}
  :uberwar {:init    backend.app/start
            :handler backend.app/repl-handler
            :destroy backend.app/stop}
  :aliases {
            "db-clean"    ["run" "-m" "backend.lein/db-clean"]
            "db-migrate"  ["run" "-m" "backend.lein/db-migrate"]
            "db-recreate" ["run" "-m" "backend.lein/db-recreate"]
            "test"        ["run" "-m" "lein-test/run-test"]
            }
  )
