(ns res-mgr-server.repo.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [migratus.core :as migratus]
            [res-mgr-server.service.env :refer [env]]
            [clojure.tools.logging :as log])
  (:import [com.zaxxer.hikari HikariDataSource HikariConfig]))

;; Datasource
(def db-spec (:db env))

(defn make-datasource []
  (let [config (HikariConfig.)]
    (.setJdbcUrl config (str "jdbc:" (:dbtype db-spec) ":" (:dbname db-spec)))
    (.setMaximumPoolSize config 10)
    (HikariDataSource. config)))

(defonce datasource (make-datasource))

;; Migratus
(def migratus-config
  {:store :database
   :migration-dir "migrations"
   :db {:datasource datasource}})

(defn init-db! []
  (log/info "Initializing database migrations...")
  (migratus/init migratus-config)
  (migratus/migrate migratus-config)
  (log/info "Database migrations completed."))

(def ^:private builder-fn {:builder-fn rs/as-unqualified-lower-maps})

;; Location
(defn create-location! [loc]
  (jdbc/with-transaction [tx datasource]
    (sql/insert! tx :location loc builder-fn)))

(defn list-locations []
  (sql/query datasource ["select * from location"] builder-fn))

(defn find-locations [criteria]
  (sql/find-by-keys datasource :location criteria builder-fn))

(defn search-locations [name-pattern]
  (sql/query datasource
             ["select * from location where name like ?" (str name-pattern "%")]
             builder-fn))

;; Tag
(defn create-tag! [tag]
  (jdbc/with-transaction [tx datasource]
    (sql/insert! tx :tag tag builder-fn)))

(defn list-tags []
  (sql/query datasource ["select * from tag"] builder-fn))

(defn search-tags [name-pattern]
  (sql/query datasource
             ["select * from tag where name like ?" (str name-pattern "%")]
             builder-fn))

;; Category
(defn create-category! [cat]
  (jdbc/with-transaction [tx datasource]
    (sql/insert! tx :category cat builder-fn)))

(defn list-categories []
  (sql/query datasource ["select * from category"] builder-fn))

(defn search-categories [name-pattern]
  (sql/query datasource
             ["select * from category where name like ?" (str name-pattern "%")]
             builder-fn))

;; Item
(defn create-item! [item]
  (jdbc/with-transaction [tx datasource]
    (sql/insert! tx :item item builder-fn)))

(defn get-item [id]
  (sql/get-by-id datasource :item id builder-fn))

(defn list-items []
  (sql/query datasource ["select * from item"] builder-fn))

;; Prop
(defn create-prop! [prop]
  (jdbc/with-transaction [tx datasource]
    (sql/insert! tx :prop prop builder-fn)))

(defn get-prop [id]
  (sql/get-by-id datasource :prop id builder-fn))

(defn list-props []
  (sql/query datasource ["select * from prop"] builder-fn))

(defn search-props [name-pattern]
  (sql/query datasource
             ["select * from prop where name like ?" (str name-pattern "%")]
             builder-fn))

(defn search-props-by-category [category-id name-pattern]
  (sql/query datasource
             ["select p.* from prop p
               join category_prop_map cpm on p.id = cpm.prop_id
               where cpm.category_id = ? and p.name like ?"
              category-id (str name-pattern "%")]
             builder-fn))

(defn search-props-by-item [item-id name-pattern]
  (sql/query datasource
             ["select p.* from prop p
               join category_prop_map cpm on p.id = cpm.prop_id
               join item i on i.category_id = cpm.category_id
               where i.id = ? and p.name like ?"
              item-id (str name-pattern "%")]
             builder-fn))

;; Item Tag Map
(defn add-item-tag! [item-id tag-id]
  (jdbc/with-transaction [tx datasource]
    (sql/insert! tx :item_tag_map {:item_id item-id :tag_id tag-id} builder-fn)))

(defn get-item-tags [item-id]
  (sql/query datasource
             ["select t.* from tag t join item_tag_map m on t.id = m.tag_id where m.item_id = ?" item-id]
             builder-fn))

(defn find-items-by-tag [tag-id]
  (sql/query datasource
             ["select i.* from item i
               join item_tag_map m on i.id = m.item_id
               where m.tag_id = ?"
              tag-id]
             builder-fn))

;; Item Location Map
(defn add-item-location! [item-id location-id]
  (jdbc/with-transaction [tx datasource]
    (sql/insert! tx :item_location_map {:item_id item-id :location_id location-id} builder-fn)))

(defn get-item-locations [item-id]
  (sql/query datasource
             ["select l.* from location l join item_location_map m on l.id = m.location_id where m.item_id = ?" item-id]
             builder-fn))

(defn find-items-by-location [location-id]
  (sql/query datasource
             ["select i.* from item i
               join item_location_map m on i.id = m.item_id
               where m.location_id = ?"
              location-id]
             builder-fn))
