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
(ns sixsq.slipstream.run-app-scale-test
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
                                        is-uuid
                                        run-uuid-from-run-url
                                        set-run-uuid
                                        with-dont-ignore-abort]]))

(http-quiet!)


(use-fixtures :each fixture-terminate)


(deftest test-deploy-scale-terminate

  (let [{:keys [username password endpoint app-uri
                comp-name connector-name insecure?]} (get-config)
        deploy-params-map (-> {:scalable                       true
                               (str comp-name ":multiplicity") 0}
                              (cond-> connector-name (assoc (str comp-name ":cloudservice") connector-name)))]

    (testing "Authenticate"
      (a/set-context! {:serviceurl endpoint :insecure? (boolean insecure?)})
      (a/login! username password (str endpoint "/" a/login-resource))
      (is (:cookie a/*context*)))

    (testing "Start scalable run."
      (let [run-url (p/deploy app-uri deploy-params-map)]
        (is (is-url run-url))
        (let [run-uuid (run-uuid-from-run-url run-url)]
          (is (is-uuid run-uuid))
          (r/contextualize! (assoc a/*context* :diid run-uuid))
          (set-run-uuid run-uuid)))
      (is (true? (r/scalable?)))
      (is (= 0 (r/get-multiplicity comp-name)))
      (is (true? (with-dont-ignore-abort (r/wait-ready)))))

    (testing "Scale up by 2."
      (is (= (inst-names-range comp-name 1 3) (r/scale-up comp-name 2)))
      (is (= 2 (r/get-multiplicity comp-name)))
      (is (= '("1" "2") (r/get-comp-ids comp-name)))
      (is (true? (with-dont-ignore-abort (r/wait-provisioning))))
      (is (true? (with-dont-ignore-abort (r/wait-ready)))))

    ;; FIXME: Scaling down an application is not working correctly.
    #_(testing "Scale down instance ID 1."
        (is (true? (r/action-success? (r/action-scale-down-at comp-name [1]))))
        (is (= 1 (r/get-multiplicity comp-name)))
        (is (= '("2") (r/get-comp-ids comp-name)))
        (is (true? (with-dont-ignore-abort (r/wait-ready)))))

    #_(testing "Scale down by 1."
        (is (true? (r/action-success? (r/action-scale-down-by comp-name 1))))
        (is (= 0 (r/get-multiplicity comp-name)))
        (is (= [] (r/get-comp-ids comp-name)))
        (is (true? (with-dont-ignore-abort (r/wait-ready)))))

    (testing "Terminate deployment."
      (is (= 204 (:status (r/terminate))))
      (is (true? (#'sixsq.slipstream.client.run-impl.lib.run/wait-state (r/run-uuid) "Done"))))))
