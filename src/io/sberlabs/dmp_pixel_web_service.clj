(ns io.sberlabs.dmp-pixel-web-service
  (:require [clojure.tools.logging :as log]
            [compojure.core :as compojure]
            [io.sberlabs.dmp-pixel-web-core :as core]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :as tk-services]))

(defservice pixel-web-service
  [[:ConfigService get-in-config]
   [:WebroutingService add-ring-handler get-route]
   PixelService LoggerService]

  (init [this context]
    (log/info "Initializing pixel webservice")
    (let [url-prefix (get-route this)]
      (add-ring-handler
        this
        (compojure/context url-prefix []
                           (core/app (tk-services/get-service this :PixelService)
                                     (tk-services/get-service this :LoggerService))))
      (assoc context :url-prefix url-prefix)))

  (start [this context]
         (let [host (get-in-config [:webserver :host])
               port (get-in-config [:webserver :port])
               url-prefix (get-route this)]
              (log/infof "Pixel web service started; visit http://%s:%s%s to check it out!"
                         host port url-prefix))
         context))
