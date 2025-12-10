(ns res-mgr-server.controller.main
  (:require [clojure.tools.logging :as logging]
            [ring.util.response :refer [response created not-found bad-request]]
            [res-mgr-server.repo.db :as db]
            [clojure.spec.alpha :as s]
            [clojure.string :refer [blank?]])
  (:import [java.util UUID])
  (:gen-class))

;; Specs
(defn uuid-str? [s]
  (try (UUID/fromString s) true (catch Exception _ false)))

(s/def ::id uuid-str?)
(s/def ::name (s/and string? #(not (blank? %))))
(s/def ::client_type string?)
(s/def ::path string?)
(s/def ::category_id uuid-str?)
(s/def ::is_safe (s/or :boolean boolean? :integer integer?)) ;; SQLite uses integer for boolean
(s/def ::type string?)

(s/def ::location (s/keys :req-un [::name ::path ::client_type] :opt-un [::id]))
(s/def ::tag (s/keys :req-un [::name] :opt-un [::id]))
(s/def ::category (s/keys :req-un [::name] :opt-un [::id]))
(s/def ::item (s/keys :req-un [::category_id ::is_safe] :opt-un [::id]))
(s/def ::prop (s/keys :req-un [::name ::type] :opt-un [::id]))

(defn validate [spec data]
  (if (s/valid? spec data)
    nil
    (s/explain-str spec data)))

(defn- ensure-id [m]
  (if (:id m)
    m
    (assoc m :id (str (UUID/randomUUID)))))

(defn health-check [_]
  (response {:status "ok"}))

;; Location
(defn create-location [req]
  (let [body (:body req)]
    (if-let [error (validate ::location body)]
      (bad-request {:error error})
      (let [loc (ensure-id body)]
        (db/create-location! loc)
        (created (str "/locations/" (:id loc)) loc)))))

(defn list-locations [req]
  (let [query (-> req :params :q)]
    (response
     (if query
       (db/search-locations query)
       (db/list-locations)))))

(defn find-items-by-location [req]
  (let [id (-> req :params :id)]
    (if (uuid-str? id)
      (response (db/find-items-by-location id))
      (bad-request {:error "Invalid UUID"}))))

;; Tag
(defn create-tag [req]
  (let [body (:body req)]
    (if-let [error (validate ::tag body)]
      (bad-request {:error error})
      (let [tag (ensure-id body)]
        (db/create-tag! tag)
        (created (str "/tags/" (:id tag)) tag)))))

(defn list-tags [req]
  (let [query (-> req :params :q)]
    (response
     (if query
       (db/search-tags query)
       (db/list-tags)))))

(defn find-items-by-tag [req]
  (let [id (-> req :params :id)]
    (if (uuid-str? id)
      (response (db/find-items-by-tag id))
      (bad-request {:error "Invalid UUID"}))))

;; Category
(defn create-category [req]
  (let [body (:body req)]
    (if-let [error (validate ::category body)]
      (bad-request {:error error})
      (let [cat (ensure-id body)]
        (db/create-category! cat)
        (created (str "/categories/" (:id cat)) cat)))))

(defn list-categories [req]
  (let [query (-> req :params :q)]
    (response
     (if query
       (db/search-categories query)
       (db/list-categories)))))

(defn search-props-by-category [req]
  (let [id (-> req :params :id)
        query (-> req :params :q)]
    (if (uuid-str? id)
      (response (db/search-props-by-category id (or query "")))
      (bad-request {:error "Invalid UUID"}))))

;; Item
(defn create-item [req]
  (let [body (:body req)]
    (if-let [error (validate ::item body)]
      (bad-request {:error error})
      (let [item (ensure-id body)]
        (db/create-item! item)
        (created (str "/items/" (:id item)) item)))))

(defn get-item [req]
  (let [id (-> req :params :id)]
    (if (uuid-str? id)
      (let [item (db/get-item id)]
        (if item
          (response item)
          (not-found {:error "Item not found"})))
      (bad-request {:error "Invalid UUID"}))))

(defn list-items [req]
  (response (db/list-items)))

(defn search-props-by-item [req]
  (let [id (-> req :params :id)
        query (-> req :params :q)]
    (if (uuid-str? id)
      (response (db/search-props-by-item id (or query "")))
      (bad-request {:error "Invalid UUID"}))))

;; Prop
(defn create-prop [req]
  (let [body (:body req)]
    (if-let [error (validate ::prop body)]
      (bad-request {:error error})
      (let [prop (ensure-id body)]
        (db/create-prop! prop)
        (created (str "/props/" (:id prop)) prop)))))

(defn list-props [req]
  (let [query (-> req :params :q)]
    (response
     (if query
       (db/search-props query)
       (db/list-props)))))

;; Item Relations
(defn add-item-tag [req]
  (let [id (-> req :params :id)
        tag-id (-> req :body :tag_id)]
    (if (and (uuid-str? id) (uuid-str? tag-id))
      (do
        (db/add-item-tag! id tag-id)
        (created (str "/items/" id "/tags/" tag-id) {:item_id id :tag_id tag-id}))
      (bad-request {:error "Invalid UUIDs"}))))

(defn get-item-tags [req]
  (let [id (-> req :params :id)]
    (if (uuid-str? id)
      (response (db/get-item-tags id))
      (bad-request {:error "Invalid UUID"}))))

(defn add-item-location [req]
  (let [id (-> req :params :id)
        location-id (-> req :body :location_id)]
    (if (and (uuid-str? id) (uuid-str? location-id))
      (do
        (db/add-item-location! id location-id)
        (created (str "/items/" id "/locations/" location-id) {:item_id id :location_id location-id}))
      (bad-request {:error "Invalid UUIDs"}))))

(defn get-item-locations [req]
  (let [id (-> req :params :id)]
    (if (uuid-str? id)
      (response (db/get-item-locations id))
      (bad-request {:error "Invalid UUID"}))))

