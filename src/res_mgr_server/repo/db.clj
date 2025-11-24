(ns res-mgr-server.repo.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource))
  (:gen-class))

;; Protocol for database operations
(defprotocol IDatabase
  "Common interface for database CRUD operations"
  (get-connection [this] "Get a database connection pool")
  (select* [this query opts] "Execute a SELECT query")
  (insert* [this table data] "Insert a row into table")
  (update* [this table data where-clause] "Update rows in table")
  (delete* [this table where-clause] "Delete rows from table"))

;; PostgreSQL implementation
(defrecord PostgresDB [pool]
  IDatabase
  (get-connection [_] pool)
  (select* [_ query opts]
    (jdbc/execute! pool query opts))
  (insert* [_ table data]
    (jdbc/insert! pool table data))
  (update* [_ table data where-clause]
    (jdbc/update! pool table data where-clause))
  (delete* [_ table where-clause]
    (jdbc/delete! pool table where-clause)))

;; SQLite implementation
(defrecord SqliteDB [pool]
  IDatabase
  (get-connection [_] pool)
  (select* [_ query opts]
    (jdbc/execute! pool query opts))
  (insert* [_ table data]
    (jdbc/insert! pool table data))
  (update* [_ table data where-clause]
    (jdbc/update! pool table data where-clause))
  (delete* [_ table where-clause]
    (jdbc/delete! pool table where-clause)))

;; Validation functions
(defn- validate-postgres-conf [config]
  (let [required-keys [:host :port :dbtype :dbname :username :password]]
    (doseq [key required-keys]
      (when (nil? (get config key))
        (throw (IllegalArgumentException. (str "Missing required PostgreSQL config: " key)))))))

(defn- validate-sqlite-conf [config]
  (let [required-keys [:dbtype :database]]
    (doseq [key required-keys]
      (when (nil? (get config key))
        (throw (IllegalArgumentException. (str "Missing required SQLite config: " key)))))))

;; Factory function to create appropriate DB instance based on config
(defn create-db [db-config]
  (let [dbtype (keyword (:dbtype db-config))]
    (case dbtype
      :postgres (do
                  (validate-postgres-conf db-config)
                  (let [pool (connection/->pool com.zaxxer.hikari.HikariDataSource db-config)]
                    (->PostgresDB pool)))
      :sqlite (do
                (validate-sqlite-conf db-config)
                (let [pool (connection/->pool com.zaxxer.hikari.HikariDataSource db-config)]
                  (->SqliteDB pool)))
      (throw (IllegalArgumentException. (str "Unsupported database type: " dbtype))))))

;; Default configuration - TODO support environ
(def default-db-conf
  {:host "localhost"
   :port "3000"
   :dbtype "postgres"
   :dbname "res_mgr_server"
   :username "postgres"
   :password "password"
   :dataSourceProperties {:socketTimeout 30}})

;; Create default instance
(def db (create-db default-db-conf))