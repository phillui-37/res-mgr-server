(ns res-mgr-server.controller.main
  (:require [clojure.tools.logging :as logging]
            [ring.util.response :refer [response]])
  (:gen-class))

(defn handler [request]
  (logging/error "request received")
  (logging/warn (:request-method request) (:uri request))
  (response {:test "ing"}))