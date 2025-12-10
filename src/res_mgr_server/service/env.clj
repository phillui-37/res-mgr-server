(ns res-mgr-server.service.env
  (:require [clojure.tools.logging :as logging])
  (:gen-class))

(defrecord ServerEnv [host port])
(defrecord DbEnv [dbtype dbname])
(defrecord AppEnv [server db])

(defn- get-env [key default]
  (or (System/getenv key) default))

(defn- parse-int [s default]
  (try
    (Integer/parseInt s)
    (catch NumberFormatException e
      (logging/error "Invalid integer for env var:" s "Using default:" default)
      default)))

(def env (let [server (ServerEnv.
                       (get-env "SERVER_HOST" "localhost")
                       (parse-int (get-env "SERVER_PORT" "8080") 8080))
               db (DbEnv.
                   (get-env "DB_TYPE" "sqlite")
                   (get-env "DB_NAME" "res_mgr_db.sqlite"))]
           (AppEnv. server db)))
