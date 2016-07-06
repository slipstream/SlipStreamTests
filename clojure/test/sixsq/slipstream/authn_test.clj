(ns sixsq.slipstream.authn-test
  (:require [clojure.test :refer :all]
            [clojure.test.junit :refer :all]
            [clojure.string :refer [starts-with?]]
            [sixsq.slipstream.test-base :refer [get-config]]
            [sixsq.slipstream.client.api.authn :as a]))

(def config (get-config))
(def username (:username config))
(def password (:password config))
(def serviceurl (:serviceurl config))
(def insecure (:insecure? config))

;; TODO: use cookie handling library to test content of cookie.
(deftest test-authn
  (let [cookie (a/with-context {:insecure? insecure}
                 (a/login! username password (a/to-login-url serviceurl)))]
    (is (not (nil? cookie)))
    (is (starts-with? cookie "com.sixsq.slipstream.cookie"))
    (is (.endsWith cookie "Path=/"))))

