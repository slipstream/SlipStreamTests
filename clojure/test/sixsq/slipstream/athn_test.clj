(ns sixsq.slipstream.athn-test
  (:require [clojure.test :refer :all]
            [clojure.test.junit :refer :all]
            [clojure.string :refer [starts-with?]]
            [sixsq.slipstream.test-base :refer [get-config]]
            [sixsq.slipstream.client.api.authn :as a]))

(def config (get-config))
(def username (:username config))
(def password (:password config))
(def serviceurl (:serviceurl config))

(deftest test-authn
  (let [cookie (a/login! username password (a/to-login-url serviceurl))]
    (is (not (nil? cookie)))
    (is (starts-with? cookie "com.sixsq.slipstream.cookie"))
    (is (.endsWith cookie "Path=/"))
    ))

