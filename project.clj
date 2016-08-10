(defproject clojure-rest-jdbc-backend "0.1.0-SNAPSHOT"
  :description "A REST backend on JDBC written in Clojure."
  :url "https://github.com/ivos/clojure-rest-jdbc-backend"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [com.h2database/h2 "1.3.176"]
                 [org.flywaydb/flyway-core "4.0.3"]
                 [com.layerware/hugsql "0.4.7"]
                 [hikari-cp "1.7.3"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.5.1"]
                 [bouncer "1.0.0"]
                 [slingshot "0.12.2"]
                 [clj-time "0.12.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 ;[midje "1.8.3" :scope "test"]
                 ;[ring/ring-mock "0.3.0" :scope "test"]
                 ]
  :main ^:skip-aot backend.app
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[midje "1.8.3"]
                                      [ring/ring-mock "0.3.0"]]
                       :plugins      [[lein-midje "3.2"]]}}
  :plugins [[lein-ring "0.9.7"]
            [lein-uberwar "0.2.0"]]
  :ring {:init    backend.app/start
         :handler backend.app/repl-handler
         :destroy backend.app/stop}
  :uberwar {:init    backend.app/start
            :handler backend.app/repl-handler
            :destroy backend.app/stop}
  :aliases {
            "db-clean"    ["run" "-m" "backend.app/db-clean"]
            "db-migrate"  ["run" "-m" "backend.app/db-migrate"]
            "db-recreate" ["run" "-m" "backend.app/db-recreate"]
            }
  )
