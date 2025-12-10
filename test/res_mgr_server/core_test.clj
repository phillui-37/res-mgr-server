(ns res-mgr-server.core-test
  (:require [clojure.test :refer :all]
            [res-mgr-server.core :refer :all]
            [ring.mock.request :as mock]
            [res-mgr-server.route.root :refer [app]]))

(deftest health-check-test
  (testing "Health check endpoint"
    (let [response (app (mock/request :get "/health"))]
      (is (= (:status response) 200))
      (is (= (:body response) {:status "ok"})))))
