(ns res-mgr-server.route.root
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [res-mgr-server.controller.main :refer [handler]])
  (:gen-class))

(defroutes app
  (GET "/test" [] handler)
  (route/not-found "404 Not Found"))
