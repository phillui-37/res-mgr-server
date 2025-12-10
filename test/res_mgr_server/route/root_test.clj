(ns res-mgr-server.route.root-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [res-mgr-server.core :refer [middleware-wrapped-app]]
            [res-mgr-server.repo.db :as db]
            [migratus.core :as migratus]
            [clojure.data.json :as json])
  (:import [java.io File]
           [java.util UUID]
           [com.zaxxer.hikari HikariDataSource HikariConfig]))

(defn make-test-datasource [db-path]
  (let [config (HikariConfig.)]
    (.setJdbcUrl config (str "jdbc:sqlite:" db-path))
    (.setMaximumPoolSize config 1)
    (HikariDataSource. config)))

(def ^:dynamic *test-ds* nil)

(defn db-fixture [f]
  (let [db-file (File/createTempFile "res-mgr-route-test-" ".sqlite")
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

(defn parse-body [response]
  (json/read-str (:body response) :key-fn keyword))

(deftest route-test
  (testing "Location routes"
    (let [loc-id (str (UUID/randomUUID))
          response (middleware-wrapped-app
                    (-> (mock/request :post "/locations")
                        (mock/json-body {:id loc-id
                                         :name "Office"
                                         :path "/office"
                                         :client_type "web"})))]
      (is (= 201 (:status response)))
      (let [body (parse-body response)]
        (is (= loc-id (:id body)))
        (is (= "Office" (:name body)))))

    (let [response (middleware-wrapped-app (mock/request :get "/locations"))]
      (is (= 200 (:status response)))
      (let [body (parse-body response)]
        (is (= 1 (count body)))
        (is (= "Office" (:name (first body)))))))

  (testing "Tag routes"
    (let [tag-id (str (UUID/randomUUID))
          response (middleware-wrapped-app
                    (-> (mock/request :post "/tags")
                        (mock/json-body {:id tag-id :name "urgent"})))]
      (is (= 201 (:status response))))
    
    (let [response (middleware-wrapped-app (mock/request :get "/tags?q=urg"))]
      (is (= 200 (:status response)))
      (let [body (parse-body response)]
        (is (= 1 (count body)))
        (is (= "urgent" (:name (first body)))))))

  (testing "Category and Item routes"
    (let [cat-id (str (UUID/randomUUID))
          _ (middleware-wrapped-app
             (-> (mock/request :post "/categories")
                 (mock/json-body {:id cat-id :name "Tools"})))
          
          item-id (str (UUID/randomUUID))
          response (middleware-wrapped-app
                    (-> (mock/request :post "/items")
                        (mock/json-body {:id item-id
                                         :category_id cat-id
                                         :is_safe 1})))]
      (is (= 201 (:status response)))
      
      (let [get-resp (middleware-wrapped-app (mock/request :get (str "/items/" item-id)))]
        (is (= 200 (:status get-resp)))
        (let [body (parse-body get-resp)]
          (is (= item-id (:id body)))
          (is (= cat-id (:category_id body)))))))

  (testing "Item Relations"
    (let [cat-id (str (UUID/randomUUID))
          _ (middleware-wrapped-app
             (-> (mock/request :post "/categories")
                 (mock/json-body {:id cat-id :name "Misc"})))
          
          item-id (str (UUID/randomUUID))
          _ (middleware-wrapped-app
             (-> (mock/request :post "/items")
                 (mock/json-body {:id item-id :category_id cat-id :is_safe 1})))
          
          tag-id (str (UUID/randomUUID))
          _ (middleware-wrapped-app
             (-> (mock/request :post "/tags")
                 (mock/json-body {:id tag-id :name "red"})))
          
          add-resp (middleware-wrapped-app
                    (-> (mock/request :post (str "/items/" item-id "/tags"))
                        (mock/json-body {:tag_id tag-id})))]
      (is (= 201 (:status add-resp)))
      
      (let [tags-resp (middleware-wrapped-app (mock/request :get (str "/items/" item-id "/tags")))]
        (is (= 200 (:status tags-resp)))
        (let [body (parse-body tags-resp)]
          (is (= 1 (count body)))
          (is (= tag-id (:id (first body))))))

      (let [items-resp (middleware-wrapped-app (mock/request :get (str "/tags/" tag-id "/items")))]
        (is (= 200 (:status items-resp)))
        (let [body (parse-body items-resp)]
          (is (= 1 (count body)))
          (is (= item-id (:id (first body)))))))))

(deftest validation-test
  (testing "Invalid Location Request"
    (testing "Missing required field"
      (let [response (middleware-wrapped-app
                      (-> (mock/request :post "/locations")
                          (mock/json-body {:path "/tmp" :client_type "web"})))] ;; Missing name
        (is (= 400 (:status response)))
        (is (:error (parse-body response)))))

    (testing "Invalid field type"
      (let [response (middleware-wrapped-app
                      (-> (mock/request :post "/locations")
                          (mock/json-body {:name 123 :path "/tmp" :client_type "web"})))] ;; Name is int
        (is (= 400 (:status response)))
        (is (:error (parse-body response))))))

  (testing "Invalid Item Request"
    (testing "Invalid UUID format"
      (let [response (middleware-wrapped-app
                      (-> (mock/request :post "/items")
                          (mock/json-body {:category_id "not-a-uuid" :is_safe 1})))]
        (is (= 400 (:status response)))
        (is (:error (parse-body response))))))

  (testing "Invalid Prop Request"
    (testing "Missing type"
      (let [response (middleware-wrapped-app
                      (-> (mock/request :post "/props")
                          (mock/json-body {:name "Color"})))]
        (is (= 400 (:status response)))
        (is (:error (parse-body response)))))))
