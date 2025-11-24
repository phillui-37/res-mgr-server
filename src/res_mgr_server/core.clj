(ns res-mgr-server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params]
            [clojure.tools.logging :as logging]
            [ring.middleware.json :refer [wrap-json-response]]
            [res-mgr-server.route.root :refer [app]])
  (:gen-class))

(def port 3000)



(def middleware-wrapped-app
  (-> app
      (wrap-json-response)))

(defonce server (atom nil))

(defn start! []
  (logging/info "server start, port=" port)
  (reset! server
          (run-jetty #'middleware-wrapped-app {:port port
                                               :join? false})))

(defn -main []
  (logging/debug "main start")
  (start!))
