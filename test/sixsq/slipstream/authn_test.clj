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
  (:require
    [clojure.test :refer [deftest is testing]]
    [sixsq.slipstream.client.api.authn :as authn]
    [sixsq.slipstream.client.sync :as sync]
    [sixsq.slipstream.test-base :as base]))


(base/http-quiet!)


(deftest test-authn
  (let [{:keys [username password endpoint insecure?]} (base/get-config)]

    (testing "test configuration"
      (is username)
      (is password)
      (is endpoint))

    (let [cep-url (str endpoint "/api/cloud-entry-point")
          client-sync (sync/instance cep-url {:insecure? (boolean insecure?)})
          session (authn/login client-sync {:href     "session-template/internal"
                                            :username username
                                            :password password})]

      (testing "client authentication"
        (is (= 201 (:status session)))
        (is (authn/authenticated? client-sync))
        (is (= 200 (:status (authn/logout client-sync))))
        (is (not (authn/authenticated? client-sync)))))))
