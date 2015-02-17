(ns io.sberlabs.dmp-logger-service)

(defprotocol LoggerService
  (log-data [this data]))

