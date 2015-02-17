(ns io.sberlabs.kafka
  (:require [clojure.tools.logging :as log]
            [clojure.walk          :refer [stringify-keys]])
  (:import [kafka.javaapi.producer Producer                   ]
           [kafka.producer         KeyedMessage ProducerConfig]
           [java.util              Properties                 ]
           [com.google.common.hash Hashing])
  (:gen-class))

(defn- hashmap-to-properties
  ^java.util.Properties [^clojure.lang.PersistentArrayMap h]
  (doto (Properties.)
    (.putAll (stringify-keys h))))

(defn producer-connector
  [^clojure.lang.PersistentArrayMap h]
  (let [config (ProducerConfig. (hashmap-to-properties h))]
    (Producer. config)))

(defn message
  [topic key value]
  (KeyedMessage. topic key value))

(defn produce
  [^Producer producer ^KeyedMessage message]
  (.send producer message))

