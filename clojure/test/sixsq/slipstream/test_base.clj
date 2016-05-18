(ns sixsq.slipstream.test-base
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]))

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
