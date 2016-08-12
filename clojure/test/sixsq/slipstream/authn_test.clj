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
            [clojure.string :as s]
            [clj-http.cookies :refer [decode-cookie]]
            [clj-jwt.core  :refer [str->jwt]]
            [sixsq.slipstream.test-base :refer [get-config http-quiet!]]
            [sixsq.slipstream.client.api.authn :as a]))

(http-quiet!)

(def config (get-config))
(def username (:username config))
(def password (:password config))
(def endpoint (:endpoint config))
(def insecure (:insecure? config))

(deftest test-authn
  (let [cookie (a/with-context {:insecure? insecure}
                 (a/login! username password (a/to-login-url endpoint)))]
    (is (not (nil? cookie)))
    (let [cookie-decoded (decode-cookie cookie)]
      (is (= 2 (count cookie-decoded)))
        (let [[cname cmeta] cookie-decoded]
          (is (= "com.sixsq.slipstream.cookie" cname))
          (is (= "/" (:path cmeta)))
          (is (s/starts-with? (:value cmeta) "token="))
          (let [token (-> (:value cmeta)
                          (s/replace #"^token=" "")
                          str->jwt)]
            (is (contains? token :claims))
            (let [claims (:claims token)]
              (is (= username (:com.sixsq.identifier claims)))
              (is (not (empty? (:com.sixsq.roles claims))))
              (is (< (quot (System/currentTimeMillis) 1000) (:exp claims)))))))))

