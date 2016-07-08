(ns sixsq.slipstream.test-base
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [taoensso.timbre :as log]
    [cemerick.url :refer [url]]
    [sixsq.slipstream.client.api.lib.run :as lr]
    [sixsq.slipstream.client.api.authn :as a])
  (:import (java.util UUID)
           (java.net MalformedURLException)))


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

;; configuration releated.
(def config-fn "test-config.edn")
(def config-path
  (if-let [f (io/resource config-fn)] (.getPath f)))

(defn read-config
  [cf]
  (-> cf
      slurp
      (edn/read-string)))

(defn get-config
  []
  (read-config config-path))

;; http related.
(defn http-quiet!
  []
  (log/merge-config! {:ns-blacklist ["kvlt.*"]}))

(defmacro with-dont-ignore-abort
  [& body]
  `(a/with-context {:query-params {:ignoreabort "false"}} ~@body))

(def ^:dynamic *run-uuid* nil)
(defn set-run-uuid
  [ru]
  (alter-var-root #'*run-uuid* (constantly ru)))

;; Fixtures.
(defn fixture-terminate [f]
  (set-run-uuid nil)
  (f)
  (if-not (nil? *run-uuid*)
    (try (lr/terminate *run-uuid*) (catch Exception _))))