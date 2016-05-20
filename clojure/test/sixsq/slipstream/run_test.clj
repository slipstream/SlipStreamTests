(ns sixsq.slipstream.run-test
  (:require [clojure.test :refer :all]
            [clojure.test.junit :refer :all]
            [clojure.string :refer [starts-with?]]
            [cemerick.url :refer [url]]
            [sixsq.slipstream.test-base :refer [get-config]]
            [sixsq.slipstream.client.api.authn :as a]
            [sixsq.slipstream.client.api.lib.app :as p]
            [sixsq.slipstream.client.api.run :as r]
            [sixsq.slipstream.client.api.lib.run :as lr]
            )
  (:import (java.util UUID)
           (java.net MalformedURLException)))

(def config (get-config))
(def username (:username config))
(def password (:password config))
(def serviceurl (:serviceurl config))
(def app-uri (:app-uri config))
(def comp-name (:comp-name config))

(def ^:dynamic *run-uuid* nil)
(defn set-run-uuid
  [ru]
  (alter-var-root #'*run-uuid* (constantly ru)))

;; move to sixsq.slipstream.client.api.utils.utils
(defn is-uuid
  [u]
  (try
    (do
      (UUID/fromString u)
      true)
    (catch IllegalArgumentException e
      false)))
(defn is-url
  [u]
  (try
    (do (url u)
        true)
    (catch MalformedURLException e
      false)))

;; move to sixsq.slipstream.client.api.lib.run
(defn inst-names-range
  [comp-name start stop]
  (vec (map #(str comp-name "." %) (range start stop))))
(defn run-uuid-from-run-url
  [run-url]
  (-> run-url
      clojure.string/trim
      (clojure.string/split #"/")
      last
      clojure.string/trim))

;;
;; Fixtures.
(defn fixture-terminate [f]
  (f)
  (if-not (nil? *run-uuid*) (lr/terminate *run-uuid*))
  )

(use-fixtures :each fixture-terminate)

;;
;; Tests.
(deftest test-deploy-scale-terminate

  (testing "Authenticate: get and validate cookie."
    (let [cookie (a/login! username password (a/to-login-url serviceurl))]
      (is (not (nil? cookie)))
      (is (starts-with? cookie "com.sixsq.slipstream.cookie"))
      (is (.endsWith cookie "Path=/"))
      ))

  (testing "Start scalable run."
    (let [run-url (p/deploy app-uri {:scalable                       true
                                     (str comp-name ":multiplicity") 0})]
      (is (is-url run-url))
      (let [run-uuid (run-uuid-from-run-url run-url)]
        (is (is-uuid run-uuid))
        (r/contextualize! (assoc a/*context* :diid run-uuid))
        (set-run-uuid run-uuid))
      )
    (is (true? (r/scalable?)))
    (is (= 0 (r/get-multiplicity comp-name)))
    (is (true? (r/wait-ready)))
    )

  (testing "Scale up by 2."
    (is (= (inst-names-range comp-name 1 3) (r/scale-up comp-name 2)))
    (is (= 2 (r/get-multiplicity comp-name)))
    (is (= '("1" "2") (r/get-comp-ids comp-name)))
    (is (true? (r/wait-ready)))
    )

  (testing "Scale down instance ID 1."
    (is (true? (r/action-success? (r/action-scale-down-at comp-name [1]))))
    (is (= 1 (r/get-multiplicity comp-name)))
    (is (= '("2") (r/get-comp-ids comp-name)))
    )

  (testing "Scale down by 1."
    (is (true? (r/action-success? (r/action-scale-down-by comp-name 1))))
    (is (= 0 (r/get-multiplicity comp-name)))
    (is (= [] (r/get-comp-ids comp-name)))
    )

  (testing "Terminate deployment."
    (is (= 204 (:status (r/terminate))))
    )
  )

