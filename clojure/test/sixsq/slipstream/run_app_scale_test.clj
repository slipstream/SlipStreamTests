(ns sixsq.slipstream.run-app-scale-test
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
(def serviceurl (:serviceurl config))
(def app-uri (:app-uri config))
(def comp-name (:comp-name config))
(def connector-name (:connector-name config))
(def insecure (:insecure? config))

(a/set-context! {:insecure? insecure})

(def deploy-params-map
  (-> {:scalable                       true
       (str comp-name ":multiplicity") 0}
      (cond-> connector-name (assoc (str comp-name ":cloudservice") connector-name))))

(use-fixtures :each fixture-terminate)

;;
;; Tests.
(deftest test-deploy-scale-terminate

  (testing "Authenticate: get and validate cookie."
    (let [cookie (a/login! username password (a/to-login-url serviceurl))]
      (is (not (nil? cookie)))
      (is (starts-with? cookie "com.sixsq.slipstream.cookie"))
      (is (.endsWith cookie "Path=/"))))

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
    (is (true? (with-dont-ignore-abort (r/wait-ready)))))

  (testing "Scale down instance ID 1."
    (is (true? (r/action-success? (r/action-scale-down-at comp-name [1]))))
    (is (= 1 (r/get-multiplicity comp-name)))
    (is (= '("2") (r/get-comp-ids comp-name))))

  (testing "Scale down by 1."
    (is (true? (r/action-success? (r/action-scale-down-by comp-name 1))))
    (is (= 0 (r/get-multiplicity comp-name)))
    (is (= [] (r/get-comp-ids comp-name))))

  (testing "Terminate deployment."
    (is (= 204 (:status (r/terminate))))
    #_(is (true? (r/wait-done)))))

