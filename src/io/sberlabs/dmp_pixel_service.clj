(ns io.sberlabs.dmp-pixel-service
  (:require [clojure.tools.logging         :as log]
            [taoensso.carmine              :as car]
            [clj-time.core                 :as t]
            [clj-time.coerce               :as c]
            [base64-clj.core               :as base64]
            [ring.util.response            :refer :all]
            [clojure.tools.html-utils      :refer [format-cookie-date]]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]))

;; Helper functions
(defn- repl [tmpl var val] (.replaceAll tmpl (str "\\{" var "\\}") val))
(defn- ten-years-from-now [] (format-cookie-date (c/to-date (t/plus (t/now) (t/years 10)))))

;; Service definition
(defprotocol PixelService
  (serve-pixel [this uuid])
  (sync-cookie [this uuid])
  (cookie-synced? [this uuid]))

(defservice pixel-service
  PixelService
  [[:ConfigService get-in-config]]

  (init
   [this context]
   (log/info "Initializing pixel service")
   (assoc context :pixel-bytes (base64/decode (get-in-config [:pixel :pixel-base64]))))

  (start
   [this context]
   (log/info "Starting pixel service")
   context)

  (stop
   [this context]
   (log/info "Shutting down pixel service")
   context)

  (serve-pixel
   [this uuid]
   (-> (response ((service-context this) :pixel-bytes))
       (content-type (get-in-config [:pixel :content-type]))
       (set-cookie (get-in-config [:pixel :cookie-name]) uuid {:expires (ten-years-from-now)})))

  (sync-cookie
   [this uuid]
   (let [redis-server {:pool {} :spec (get-in-config [:redis])}
         url (str "http://" (repl (get-in-config [:pixel :cookie-sync-url-tmpl]) "id" uuid))
         key (repl (get-in-config [:pixel :cookie-sync-key-tmpl]) "id" uuid)]
     (car/wcar redis-server (car/setex key (get-in-config [:pixel :sync-expiration-time]) 1))
     (-> (redirect url)
         (set-cookie (get-in-config [:pixel :cookie-name]) uuid {:expires (ten-years-from-now)}))))

  (cookie-synced?
   [this uuid]
   (let [redis-server {:pool {} :spec (get-in-config [:redis])}
         key (repl (get-in-config [:pixel :cookie-sync-key-tmpl]) "id" uuid)
         res (car/wcar redis-server (car/get key))]
     (not (nil? res)))))
