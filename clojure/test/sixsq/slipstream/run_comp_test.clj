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
(ns sixsq.slipstream.run-comp-test
  (:require [clojure.test :refer :all]
            [clojure.string :refer [starts-with?]]
            [sixsq.slipstream.test-base :refer [get-config http-quiet!
                                                with-dont-ignore-abort
                                                fixture-terminate set-run-uuid
                                                is-uuid is-url run-uuid-from-run-url
                                                inst-names-range]]
            [sixsq.slipstream.client.api.authn :as a]
            [sixsq.slipstream.client.api.lib.app :as p]
            [sixsq.slipstream.client.api.run :as r]))

(http-quiet!)

(def config (get-config))
(def username (:username config))
(def password (:password config))
(def endpoint (:endpoint config))
(def comp-uri (:comp-uri config))
(def connector-name (:connector-name config))
(def insecure (:insecure? config))

(a/set-context! {:insecure? insecure})

(def deploy-params-map
  (-> {}
      (cond-> connector-name (assoc "cloudservice" connector-name))))

(use-fixtures :each fixture-terminate)

;;
;; Tests.
(deftest test-component-deploy-terminate

  (testing "Authenticate: get and validate cookie."
    (let [cookie (a/login! username password (a/to-login-url endpoint))]
      (is (not (nil? cookie)))
      (is (starts-with? cookie "com.sixsq.slipstream.cookie"))
      (is (re-matches #".*Path=/.*" cookie))))

  (testing "Deploy component."
    (let [run-url (p/deploy-comp comp-uri deploy-params-map)]
      (is (is-url run-url))
      (let [run-uuid (run-uuid-from-run-url run-url)]
        (is (is-uuid run-uuid))
        (r/contextualize! (assoc a/*context* :diid run-uuid))
        (set-run-uuid run-uuid)))
    (is (true? (with-dont-ignore-abort (r/wait-ready)))))

  (testing "Terminate deployment."
    (is (= 204 (:status (r/terminate))))
    #_(is (true? (r/wait-done)))))

