(ns io.sberlabs.kafka-consistent-hash-partitioner
  (:require [biscuit.core           :refer [crc32]])
  (:import  [kafka.producer         Partitioner]
            [kafka.utils            VerifiableProperties]
            [com.google.common.hash Hashing])
  (:gen-class
    :name ConsistentHashPartitioner
    :implements [kafka.producer.Partitioner]
    :methods [[partition [String int] int]]
    :init init
    :constructors {[kafka.utils.VerifiableProperties] []}
    ))

(defn -init
  [^kafka.utils.VerifiableProperties props]
  [[] []])

(defn -partition
  [_ key num-partitions]
  (Hashing/consistentHash (crc32 key) num-partitions))
