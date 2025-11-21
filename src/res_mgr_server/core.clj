(ns res-mgr-server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params]
            [clojure.tools.logging :as logging])
  (:gen-class))

(def port 3000)

(defn handler [request]
  (logging/error "request received")
  (logging/warn (:request-method request) (:uri request))
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(def app
  (-> #'handler))

(defonce server (atom nil))

(defn start! []
  (logging/info "server start, port=" port)
  (reset! server
          (run-jetty #'app {:port port
                            :join? false})))

(defn -main []
  (logging/debug "main start")
  (start!))
