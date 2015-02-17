(ns io.sberlabs.dmp-avro-schema-repo-service
  (:require [clojure.tools.logging             :as log]
            [abracad.avro                      :as avro]
            [cheshire.core                     :as json]
            [org.httpkit.client                :as http]
            [puppetlabs.trapperkeeper.core     :refer [defservice]]))

(defprotocol AvroSchemaRepoService
  (register-schema [this repo-subj schema]))

(defn- parse-int [s] (Integer. (re-find #"[0-9]*" s)))

;; TODO: throw exceptions on failures
(defn- register-schema-in-repo
  [repo-url repo-subj schema]
  (let [register-url (str repo-url "/" repo-subj "/register")
        schema-string (json/generate-string schema)
        options {:timeout 200
                 :headers {"Content-Type" "text/plain"}
                 :body schema-string}
        {:keys [status body error] :as resp} @(http/put register-url options)]
    (if error
      (log/error "Failed in register scheme, exception is" error)
      (if (== status 404)
        (let [subject-url (str repo-url "/" repo-subj)
              options {:timeout 200
                       :headers {"Content-Type" "application/x-www-form-urlencoded"}}
              {:keys [status error] :as resp} @(http/put subject-url options)]
          (log/infof "Subject %s not found, trying to create it..." repo-subj)
          (if error
            (log/error "Failed in register subject %s, error is %s" repo-subj error)
            (if (== status 200)
              (recur repo-url repo-subj schema)
              (log/error "Failed to register subject %s, status is %s" repo-subj status))))
        (do
          (log/infof "Register schema status is %s, schema-id is %s" status body)
          {:schema-id (parse-int body)
           :parsed-schema (avro/parse-schema schema-string)})))))

(defservice avro-schema-repo-service
  "Interface to avro schema repository"
  AvroSchemaRepoService
  [[:ConfigService get-in-config]]

  (init
   [this context]
   (log/info "Initializing avro schema repository service")
   context)

  (start
   [this context]
   (log/info "Starting avro schema repository service")
   context)

  (stop
   [this context]
   (log/info "Shutting down avro schema repository service")
   context)

  (register-schema
   [this repo-subj schema]
   (register-schema-in-repo (get-in-config [:avro :repo-url]) repo-subj schema)))
