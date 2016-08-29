(defproject clojure-rest-jdbc-backend "0.1.0-SNAPSHOT"
  :description "A REST backend on JDBC written in Clojure."
  :url "https://github.com/ivos/clojure-rest-jdbc-backend"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.clojure/data.codec "0.1.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.h2database/h2 "1.3.176"]
                 [com.layerware/hugsql "0.4.7"]
                 [hikari-cp "1.7.3"]
                 [org.flywaydb/flyway-core "4.0.3"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [compojure "1.5.1"]
                 [slingshot "0.12.2"]
                 [clj-time "0.12.0"]
                 [camel-snake-kebab "0.4.0"]
                 [org.flatland/ordered "1.5.4"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 ]
  :main ^:skip-aot backend.main
  ;:uberjar-name "backend-standalone.jar"
  ;:uberjar-name "backend-standalone.war"
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
  :ring {:init    backend.lein-ring/init
         :handler backend.lein-ring/handler
         :destroy backend.lein-ring/destroy}
  :uberwar {:init    backend.lein-ring/init
            :handler backend.lein-ring/handler
            :destroy backend.lein-ring/destroy}
  :aliases {
            "db-clean"    ["run" "-m" "lein-run/db-clean"]
            "db-migrate"  ["run" "-m" "lein-run/db-migrate"]
            "db-recreate" ["run" "-m" "lein-run/db-recreate"]
            "db-update"   ["run" "-m" "lein-run/db-update"]
            "test"        ["run" "-m" "lein-test/run-test"]
            }
  )
