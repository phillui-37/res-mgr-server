(ns res-mgr-server.core
  (:require [clojure.tools.logging :as logging]
            [res-mgr-server.route.root :refer [app]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [res-mgr-server.service.env :refer [env]]
            [res-mgr-server.repo.db :as db])
  (:gen-class))

(def port (-> env :server :port))

(def middleware-wrapped-app
  (-> app
      (wrap-keyword-params)
      (wrap-json-body {:keywords? true})
      (wrap-json-response)))

(defonce server (atom nil))

(defn start! []
  (logging/info "server start, port=" port)
  (db/init-db!)
  (reset! server
          (run-jetty #'middleware-wrapped-app {:port port
                                               :join? false})))

(defn -main []
  (logging/debug "main start")
  (start!))
