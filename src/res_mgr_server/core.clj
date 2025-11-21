(ns res-mgr-server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params]
            [clojure.tools.logging :as logging]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]])
  (:gen-class))

(def port 3000)

(defn handler [request]
  (logging/error "request received")
  (logging/warn (:request-method request) (:uri request))
  (response {:test "ing"}))

(def app 
  (-> handler
      (wrap-json-response)))

(defonce server (atom nil))

(defn start! []
  (logging/info "server start, port=" port)
  (reset! server
          (run-jetty #'app {:port port
                            :join? false})))

(defn -main []
  (logging/debug "main start")
  (start!))
