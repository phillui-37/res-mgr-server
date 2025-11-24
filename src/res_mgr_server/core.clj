(ns res-mgr-server.core
  (:require [clojure.tools.logging :as logging]
            [res-mgr-server.route.root :refer [app]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.params])
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
