(ns io.sberlabs.dmp-logger-service-kafka
  (:require [io.sberlabs.dmp-logger-service    :refer [LoggerService]]
            [io.sberlabs.kafka                 :as kafka]
            [clojure.tools.logging             :as log]
            [clj-time.core                     :as t]
            [clj-time.coerce                   :as c]
            [bytebuffer.buff                   :refer :all]
            [abracad.avro                      :as avro]
            [biscuit.core                      :refer [crc32]]
            [puppetlabs.trapperkeeper.core     :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]])
  (:import (com.google.common.hash Hashing)))

(defn- data-parser
  [parser data]
  (reduce-kv #(assoc %1 %2 (cond
                             (= %3 :unix-time-ms) (c/to-long (t/now))
                             :else (get-in data %3)))
             {} parser))

(defn- log-to-kafka
  [parser data schema schema-id magic-byte topic producer]
  (let [log-rec (data-parser parser data)
        avro-rec (apply avro/binary-encoded schema [log-rec])
        msg-buff (byte-buffer (+ (alength avro-rec) 5))]
    (-> msg-buff
        (put-byte magic-byte)
        (put-int schema-id)
        (.put avro-rec)
        .flip)
    (kafka/produce producer (kafka/message topic (:id log-rec) (.array msg-buff)))))

(defservice logger-service
  "A Kafka-based implementation of data logging"
  LoggerService
  [[:ConfigService get-in-config]
   [:AvroSchemaRepoService register-schema]]

  (init
   [this context]
   (log/info "Initializing logger [kafka] service")
   (assoc context :kafka-producer (kafka/producer-connector (get-in-config [:kafka :properties]))))

  (start
   [this context]
   (let [kafka-topic (get-in-config [:kafka :topic])
         schema (get-in-config [:avro :schema])
         {:keys [schema-id parsed-schema]} (register-schema kafka-topic schema)]
     (log/info "Initializing logger [kafka] service")
     (-> context
      (assoc :schema-id schema-id)
      (assoc :parsed-schema parsed-schema))))

  (stop
   [this context]
   (log/info "Shutting down logger [kafka] service")
   context)

  (log-data
   [this data]
   (log-to-kafka (get-in-config [:avro :parser])
                 data
                 (:parsed-schema (service-context this))
                 (:schema-id (service-context this))
                 (get-in-config [:kafka :camus-magic-byte])
                 (get-in-config [:kafka :topic])
                 (:kafka-producer (service-context this)))))

