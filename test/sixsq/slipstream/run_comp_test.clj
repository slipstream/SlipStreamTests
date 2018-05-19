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
  (:require
    [clojure.string :refer [starts-with?]]
    [clojure.test :refer [deftest is testing use-fixtures]]
    [sixsq.slipstream.client.api.deprecated-authn :as a]
    [sixsq.slipstream.client.run :as r]
    [sixsq.slipstream.client.run-impl.lib.app :as p]
    [sixsq.slipstream.client.run-impl.lib.run]
    [sixsq.slipstream.test-base :refer [fixture-terminate
                                        get-config
                                        http-quiet!
                                        inst-names-range
                                        is-url
                                        is-uuid run-uuid-from-run-url
                                        set-run-uuid
                                        with-dont-ignore-abort]]))

(http-quiet!)


(use-fixtures :each fixture-terminate)


(deftest test-component-deploy-terminate

  (let [{:keys [username password endpoint comp-uri connector-name insecure?]} (get-config)
        deploy-params-map (-> {}
                              (cond-> connector-name (assoc "cloudservice" connector-name)))]

    (testing "Authenticate"
      (a/set-context! {:serviceurl endpoint :insecure? (boolean insecure?)})
      (a/login! username password (str endpoint "/" a/login-resource))
      (is (:cookie a/*context*)))

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
      (is (true? (#'sixsq.slipstream.client.run-impl.lib.run/wait-state (r/run-uuid) "Done"))))))

