(def ks-version "1.0.0")
(def tk-version "1.0.0")
(def tk-jetty9-version "1.0.0")

(defproject io.sberlabs/dmp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [compojure "1.3.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [puppetlabs/trapperkeeper ~tk-version]
                 [puppetlabs/trapperkeeper-webserver-jetty9 ~tk-jetty9-version]
                 [http-kit "2.1.16"]
                 [ring "1.3.2":exclusions [ring/ring-servlet org.clojure/java.classpath]]
                 [clojure-tools "1.1.2"]
                 [com.taoensso/carmine "2.9.0"]
                 [clj-time "0.9.0"]
                 [base64-clj "0.1.1"]
                 [com.damballa/abracad "0.4.11"]
                 [bytebuffer "0.2.0"]
                 [biscuit "1.0.0"]
                 [com.google.guava/guava "18.0"]
                 [cheshire "5.4.0"]
                 [org.apache.kafka/kafka_2.10 "0.8.1.1"]
                 ;; [org.clojure/tools.logging "0.3.0"]
                 [io.netty/netty-handler "5.0.0.Alpha1"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/data.json "0.2.5"]]

  :exclusions [javax.mail/mail
               javax.jms/jms
               com.sun.jdmk/jmxtools
               com.sun.jmx/jmxri
               jline/jline]

  :profiles {:dev {:dependencies [[puppetlabs/trapperkeeper ~tk-version :classifier "test" :scope "test"]
                                  [puppetlabs/kitchensink ~ks-version :classifier "test" :scope "test"]
                                  [clj-http "0.9.2"]
                                  [ring-mock "0.1.5"]]}}

  :aliases {"tk" ["trampoline" "run" "--config" "dev-resources/config.conf"]}

  :main puppetlabs.trapperkeeper.main
  :aot [puppetlabs.trapperkeeper.main io.sberlabs.kafka-consistent-hash-partitioner]
  )
