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
            [sixsq.slipstream.client.api.authn :as authn]
            [sixsq.slipstream.client.sync :as sync]))

(http-quiet!)

(def config (get-config))
(def username (:username config))
(def password (:password config))
(def endpoint (:endpoint config))
(def insecure (:insecure? config))

(deftest test-authn
  (let [client-sync (sync/instance (str endpoint "/api/cloud-entry-point"))
        session     (authn/login client-sync {:href     "session-template/internal"
                                              :username username
                                              :password password}
                                 {:insecure? insecure})]
    (is (= 201 (:status session)))
    (is (authn/authenticated? client-sync))
    (is (= 200 (:status (authn/logout client-sync))))
    (is (not (authn/authenticated? client-sync)))))
