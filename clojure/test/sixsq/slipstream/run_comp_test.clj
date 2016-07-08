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
(def serviceurl (:serviceurl config))
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
    (let [cookie (a/login! username password (a/to-login-url serviceurl))]
      (is (not (nil? cookie)))
      (is (starts-with? cookie "com.sixsq.slipstream.cookie"))
      (is (.endsWith cookie "Path=/"))))

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

