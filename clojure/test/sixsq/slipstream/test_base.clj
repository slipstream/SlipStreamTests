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

