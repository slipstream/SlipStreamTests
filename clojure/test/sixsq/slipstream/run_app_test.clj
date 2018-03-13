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
(ns sixsq.slipstream.run-app-test
  (:require [clojure.test :refer :all]
            [clojure.string :refer [starts-with?]]
            [sixsq.slipstream.test-base :refer [get-config http-quiet!
                                                with-dont-ignore-abort
                                                fixture-terminate set-run-uuid
                                                is-uuid is-url run-uuid-from-run-url
                                                inst-names-range]]
            [sixsq.slipstream.client.run :as r]
            [sixsq.slipstream.client.run-impl.lib.app :as p]
            [sixsq.slipstream.client.run-impl.lib.run]
            [sixsq.slipstream.client.api.deprecated-authn :as a]))

(http-quiet!)

(def config (get-config))
(def username (:username config))
(def password (:password config))
(def endpoint (:endpoint config))
(def comp-name (:comp-name config))
(def app-uri (:app-uri config))
(def connector-name (:connector-name config))
(def insecure (:insecure? config))

(def deploy-params-map
  (-> {}
      (cond-> connector-name (assoc (str comp-name ":cloudservice") connector-name))))

(use-fixtures :each fixture-terminate)

;;
;; Tests.
(deftest test-deploy-terminate
  (testing "Authenticate"
    (a/set-context! {:serviceurl endpoint :insecure? insecure})
    (a/login! username password (str endpoint "/" a/login-resource))
    (is (:cookie a/*context*)))

  (testing "Deploy application."
    (let [run-url (p/deploy app-uri deploy-params-map)]
      (is (is-url run-url))
      (let [run-uuid (run-uuid-from-run-url run-url)]
        (is (is-uuid run-uuid))
        (r/contextualize! (assoc a/*context* :diid run-uuid))
        (set-run-uuid run-uuid))))

  (testing "Validate deployment."
    (is (false? (r/scalable?)))
    (is (= 1 (r/get-multiplicity comp-name)))
    (is (true? (with-dont-ignore-abort (r/wait-ready)))))

  (testing "Terminate application."
    (is (= 204 (:status (r/terminate))))
    (is (true? (#'sixsq.slipstream.client.run-impl.lib.run/wait-state (r/run-uuid) "Done")))))

