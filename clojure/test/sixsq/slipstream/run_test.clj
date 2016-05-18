(ns sixsq.slipstream.run-test
  (:require [clojure.test :refer :all]
            [clojure.test.junit :refer :all]
            [clojure.string :refer [starts-with?]]
            [sixsq.slipstream.test-base :refer [get-config]]
            [sixsq.slipstream.client.api.authn :as a]
            [sixsq.slipstream.client.api.lib.run :as r]))

(def config (get-config))
(def username (:username config))
(def password (:password config))
(def serviceurl (:serviceurl config))
(def app-uri (:app-uri config))

(deftest test-deploy-terminate
  (is (starts-with?
        (a/login! username password (a/to-login-url serviceurl))
        "com.sixsq.slipstream.cookie")))

