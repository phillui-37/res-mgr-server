(defproject res-mgr-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                ;;  http
                 [ring/ring-core "1.15.3"]
                 [ring/ring-jetty-adapter "1.15.3"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.slf4j/slf4j-reload4j "2.0.17"]
                 [ring/ring-json "0.5.1"]
                 [compojure "1.7.2"]
                ;;  db
                 [com.github.seancorfield/next.jdbc "1.3.1070"]
                 [com.zaxxer/HikariCP "7.0.2"]
                 [migratus "1.6.4"]
                 [org.xerial/sqlite-jdbc "3.51.1.0"]
                 [migratus "1.6.4"]
                 [migratus-lein "0.7.3"]]
  :main ^:skip-aot res-mgr-server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true", "-Dclojure.tools.logging.factory=clojure.tools.logging.impl/log4j2-factory"]}}
  :migratus {:store :database
             :db {:dbtype "sqlite"
                  :dbname "res_mgr_db.sqlite"}})
