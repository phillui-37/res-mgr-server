(ns res-mgr-server.service.env
  (:gen-class))

(defrecord ServerEnv [host port])
(defrecord AppEnv [server])

(def env (let [server (ServerEnv.
                       (or (System/getenv "SERVER_HOST") "localhost")
                       (Integer/parseInt (or (System/getenv "SERVER_PORT") "8080")))]
           (AppEnv. server)))
