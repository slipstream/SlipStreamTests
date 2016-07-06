(ns sixsq.slipstream.run-app-test
  (:require [clojure.test :refer :all]
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
(def comp-name (:comp-name config))
(def app-uri (:app-uri config))
(def connector-name (:connector-name config))

(def deploy-params-map
  (-> {}
      (cond-> connector-name (assoc (str comp-name ":cloudservice") connector-name))))

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
  (if-not (nil? *run-uuid*)
    (try (lr/terminate *run-uuid*) (catch Exception _))))

(use-fixtures :each fixture-terminate)

;;
;; Tests.
(deftest test-deploy-terminate

  (testing "Authenticate: get and validate cookie."
    (let [cookie (a/login! username password (a/to-login-url serviceurl))]
      (is (not (nil? cookie)))
      (is (starts-with? cookie "com.sixsq.slipstream.cookie"))
      (is (.endsWith cookie "Path=/"))
      ))

  (testing "Deploy application."
    (let [run-url (p/deploy app-uri deploy-params-map)]
      (is (is-url run-url))
      (let [run-uuid (run-uuid-from-run-url run-url)]
        (is (is-uuid run-uuid))
        (r/contextualize! (assoc a/*context* :diid run-uuid))
        (set-run-uuid run-uuid))
      )
    (is (false? (r/scalable?)))
    (is (= 1 (r/get-multiplicity comp-name)))
    (is (true? (r/wait-ready)))
    )

  (testing "Terminate application."
    (is (= 204 (:status (r/terminate))))
    ;; TODO: (is (true? (r/wait-done)))
    )
  )

