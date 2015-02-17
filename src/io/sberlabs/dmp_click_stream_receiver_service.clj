(ns io.sberlabs.dmp-click-stream-receiver-service
  (:require [clojure.tools.logging         :as log]
            [clojure.core.async            :refer [chan timeout go go-loop alts!
                                                   <! >! close!]
                                           :as async]
            [clojure.data.json             :as json]
            [io.sberlabs.netty             :as netty]
            [puppetlabs.trapperkeeper.core :refer [defservice]]))

(defprotocol ClickStreamReceiverService)

(defn- free-mem
  []
  (.freeMemory (Runtime/getRuntime)))

(defn- make-client
  [logger-fn]
  (fn [r w c]
    (go (loop [msg_cnt 0]
          (let [[v ch] (alts! [r w c])
                j (json/read-str v :key-fn keyword)]
            (logger-fn j)
            (if (= 0 (mod msg_cnt 1000))
              (println msg_cnt (free-mem))))
          (recur (inc msg_cnt))))))

(defservice click-stream-receiver-service
  "Simple click stream receiver and logger"
  ClickStreamReceiverService
  [[:ConfigService get-in-config]
   [:LoggerService log-data]]

  (init
   [this context]
   (log/info "Initializing click stream service")
   context)

  (start
   [this context]
   (let [host (get-in-config [:zmq-proxy :host])
         port (get-in-config [:zmq-proxy :port])]
     (log/info "Starting click stream service")
     (netty/start host port (make-client log-data) :client)
     context))

  (stop
   [this context]
   (log/info "Shutting down click stream service")
   context))

