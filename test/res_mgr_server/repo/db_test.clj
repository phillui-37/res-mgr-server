(ns res-mgr-server.repo.db-test
  (:require [clojure.test :refer :all]
            [res-mgr-server.repo.db :as db]
            [next.jdbc :as jdbc]
            [migratus.core :as migratus])
  (:import [java.util UUID]
           [java.io File]
           [com.zaxxer.hikari HikariDataSource HikariConfig]))

(defn make-test-datasource [db-path]
  (let [config (HikariConfig.)]
    (.setJdbcUrl config (str "jdbc:sqlite:" db-path))
    (.setMaximumPoolSize config 1)
    (HikariDataSource. config)))

(def ^:dynamic *test-ds* nil)

(defn db-fixture [f]
  (let [db-file (File/createTempFile "res-mgr-test-" ".sqlite")
        db-path (.getAbsolutePath db-file)
        ds (make-test-datasource db-path)
        migratus-config {:store :database
                         :migration-dir "migrations"
                         :db {:datasource ds}}]
    (.deleteOnExit db-file)
    (try
      (migratus/init migratus-config)
      (migratus/migrate migratus-config)
      (with-redefs [db/datasource ds]
        (binding [*test-ds* ds]
          (f)))
      (finally
        (.close ds)
        (.delete db-file)))))

(use-fixtures :each db-fixture)

(deftest location-test
  (testing "create and list locations"
    (let [loc-id (str (UUID/randomUUID))
          loc {:id loc-id
               :name "Home"
               :path "/home/user"
               :client_type "desktop"}]
      (db/create-location! loc)
      (let [locs (db/list-locations)]
        (is (= 1 (count locs)))
        (is (= loc (first locs))))
      
      (testing "find location"
        (let [found (db/find-locations {:name "Home"})]
          (is (= 1 (count found)))
          (is (= loc (first found)))))

      (testing "search location"
        (let [found (db/search-locations "Ho")]
          (is (= 1 (count found)))
          (is (= loc (first found))))
        (let [not-found (db/search-locations "X")]
          (is (empty? not-found)))))))

(deftest tag-test
  (testing "create and list tags"
    (let [tag-id (str (UUID/randomUUID))
          tag {:id tag-id
               :name "important"}]
      (db/create-tag! tag)
      (let [tags (db/list-tags)]
        (is (= 1 (count tags)))
        (is (= tag (first tags))))

      (testing "search tag"
        (let [found (db/search-tags "imp")]
          (is (= 1 (count found)))
          (is (= tag (first found))))))))

(deftest category-test
  (testing "create and list categories"
    (let [cat-id (str (UUID/randomUUID))
          cat {:id cat-id
               :name "Documents"}]
      (db/create-category! cat)
      (let [cats (db/list-categories)]
        (is (= 1 (count cats)))
        (is (= cat (first cats))))

      (testing "search category"
        (let [found (db/search-categories "Doc")]
          (is (= 1 (count found)))
          (is (= cat (first found))))))))

(deftest item-test
  (let [cat-id (str (UUID/randomUUID))
        cat {:id cat-id :name "Books"}
        item-id (str (UUID/randomUUID))
        item {:id item-id
              :category_id cat-id
              :is_safe 1}]
    (db/create-category! cat)
    (db/create-item! item)

    (testing "get item"
      (let [fetched (db/get-item item-id)]
        (is (= item fetched))))

    (testing "list items"
      (let [items (db/list-items)]
        (is (= 1 (count items)))
        (is (= item (first items)))))))

(deftest prop-test
  (let [cat-id (str (UUID/randomUUID))
        cat {:id cat-id :name "Electronics"}
        prop-id (str (UUID/randomUUID))
        prop {:id prop-id
              :name "Voltage"
              :type "int"}
        item-id (str (UUID/randomUUID))
        item {:id item-id
              :category_id cat-id
              :is_safe 1}]
    
    (db/create-category! cat)
    (db/create-item! item)
    (db/create-prop! prop)
    
    (jdbc/execute! *test-ds* ["INSERT INTO category_prop_map (category_id, prop_id) VALUES (?, ?)" 
                              cat-id prop-id])

    (testing "get and list props"
      (is (= prop (db/get-prop prop-id)))
      (is (= 1 (count (db/list-props)))))

    (testing "search props"
      (is (= 1 (count (db/search-props "Vol")))))

    (testing "search props by category"
      (let [found (db/search-props-by-category cat-id "Vol")]
        (is (= 1 (count found)))
        (is (= prop (first found)))))

    (testing "search props by item"
      (let [found (db/search-props-by-item item-id "Vol")]
        (is (= 1 (count found)))
        (is (= prop (first found)))))))

(deftest item-tag-map-test
  (let [item-id (str (UUID/randomUUID))
        cat-id (str (UUID/randomUUID))
        tag-id (str (UUID/randomUUID))
        item {:id item-id :category_id cat-id :is_safe 1}
        cat {:id cat-id :name "Misc"}
        tag {:id tag-id :name "Urgent"}]
    
    (db/create-category! cat)
    (db/create-item! item)
    (db/create-tag! tag)
    
    (db/add-item-tag! item-id tag-id)

    (testing "get item tags"
      (let [tags (db/get-item-tags item-id)]
        (is (= 1 (count tags)))
        (is (= tag-id (:id (first tags))))))

    (testing "find items by tag"
      (let [items (db/find-items-by-tag tag-id)]
        (is (= 1 (count items)))
        (is (= item-id (:id (first items))))))))

(deftest item-location-map-test
  (let [item-id (str (UUID/randomUUID))
        cat-id (str (UUID/randomUUID))
        loc-id (str (UUID/randomUUID))
        item {:id item-id :category_id cat-id :is_safe 1}
        cat {:id cat-id :name "Misc"}
        loc {:id loc-id :name "Shelf" :path "/shelf" :client_type "physical"}]
    
    (db/create-category! cat)
    (db/create-item! item)
    (db/create-location! loc)
    
    (db/add-item-location! item-id loc-id)

    (testing "get item locations"
      (let [locs (db/get-item-locations item-id)]
        (is (= 1 (count locs)))
        (is (= loc-id (:id (first locs))))))

    (testing "find items by location"
      (let [items (db/find-items-by-location loc-id)]
        (is (= 1 (count items)))
        (is (= item-id (:id (first items))))))))
