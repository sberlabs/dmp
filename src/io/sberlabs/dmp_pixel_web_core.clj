(ns io.sberlabs.dmp-pixel-web-core
  (:require [io.sberlabs.dmp-pixel-service  :as pixel-svc]
            [io.sberlabs.dmp-logger-service :as logger-svc]
            [clojure.tools.logging          :as log]
            [ring.middleware.params         :refer [wrap-params]]
            [ring.middleware.cookies        :refer [wrap-cookies]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [compojure.core                 :as compojure]
            [compojure.route                :as route]))

(defn- uuid [] (str (java.util.UUID/randomUUID)))

(defn- wrap-request-logger
  [handler logger-service]
  (fn [req]
    (let [res (handler req)]
      (logger-svc/log-data logger-service  {:req req, :res res})
      res)))

(defn app
  [pixel-service logger-service]
  (-> (compojure/routes
       (compojure/GET
        "/p.png" []
        (fn [req]
          (let [visitor (:value (get (:cookies req) "sberlabspx"))]
            (log/info "Handling request for visitor:" visitor)
            (if (nil? visitor)
              (pixel-svc/sync-cookie pixel-service (uuid))
              (if (pixel-svc/cookie-synced? pixel-service visitor)
                (pixel-svc/serve-pixel pixel-service visitor)
                (pixel-svc/sync-cookie pixel-service visitor))))))
       (route/not-found "Not Found"))
      (wrap-request-logger logger-service)
      wrap-cookies
      wrap-keyword-params
      wrap-params))
