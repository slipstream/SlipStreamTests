(ns sixsq.slipstream.prepare
  (:require
    [clojure.string :as str]
    [clojure.tools.cli :as cli]
    [sixsq.slipstream.utils :as u]))


(defn usage
  [options-summary]
  (str/join \newline
            [""
             "Prepares the configuration file for functional tests."
             ""
             "Usage: prepare [options]"
             ""
             "Options:"
             options-summary
             ""]))


(defn -main
  [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args u/cli-options)]

    (cond
      (:help options) (u/success (usage summary))
      errors (u/failure (u/error-msg errors)))

    (spit "resources/test-config.edn" (prn-str options))))
