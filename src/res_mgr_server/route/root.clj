(ns res-mgr-server.route.root
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [res-mgr-server.controller.main :as controller])
  (:gen-class))

(defroutes app
  (GET "/health" [] controller/health-check)
  
  ;; Locations
  (POST "/locations" [] controller/create-location)
  (GET "/locations" [] controller/list-locations)
  (GET "/locations/:id/items" [] controller/find-items-by-location)

  ;; Tags
  (POST "/tags" [] controller/create-tag)
  (GET "/tags" [] controller/list-tags)
  (GET "/tags/:id/items" [] controller/find-items-by-tag)

  ;; Categories
  (POST "/categories" [] controller/create-category)
  (GET "/categories" [] controller/list-categories)
  (GET "/categories/:id/props" [] controller/search-props-by-category)

  ;; Items
  (POST "/items" [] controller/create-item)
  (GET "/items" [] controller/list-items)
  (GET "/items/:id" [] controller/get-item)
  (GET "/items/:id/props" [] controller/search-props-by-item)
  
  ;; Item Relations
  (POST "/items/:id/tags" [] controller/add-item-tag)
  (GET "/items/:id/tags" [] controller/get-item-tags)
  (POST "/items/:id/locations" [] controller/add-item-location)
  (GET "/items/:id/locations" [] controller/get-item-locations)

  ;; Props
  (POST "/props" [] controller/create-prop)
  (GET "/props" [] controller/list-props)

  (route/not-found "404 Not Found"))
