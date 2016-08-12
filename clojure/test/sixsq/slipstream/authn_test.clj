;; Copyright 2016, SixSq Sarl
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
;;
(ns sixsq.slipstream.authn-test
  (:require [clojure.test :refer :all]
            [clojure.test.junit :refer :all]
            [clojure.string :refer [starts-with?]]
            [sixsq.slipstream.test-base :refer [get-config http-quiet!]]
            [sixsq.slipstream.client.api.authn :as a]))

(http-quiet!)

(def config (get-config))
(def username (:username config))
(def password (:password config))
(def endpoint (:endpoint config))
(def insecure (:insecure? config))

;; TODO: use cookie handling library to test content of cookie.
(deftest test-authn
  (let [cookie (a/with-context {:insecure? insecure}
                 (a/login! username password (a/to-login-url endpoint)))]
    (is (not (nil? cookie)))
    (is (starts-with? cookie "com.sixsq.slipstream.cookie"))
    (is (.endsWith cookie "Path=/"))))

