(ns io.sberlabs.dmp-core-test
  (:require [clojure.test :refer :all]
            [io.sberlabs.dmp-core :refer :all]))

(deftest hello-test
  (testing "says hello to caller"
    (is (= "Hello, foo!" (hello "foo")))))
