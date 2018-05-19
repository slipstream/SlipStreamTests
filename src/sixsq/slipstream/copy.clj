(ns sixsq.slipstream.copy
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.tools.cli :as cli]
    [sixsq.slipstream.utils :as u]
    [taoensso.timbre :as log]))


(defn usage
  [options-summary]
  (str/join \newline
            [""
             "Copies results to given directory."
             ""
             "Usage: copy [options]"
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

    (let [{:keys [results-dir connector-name]} options]
      (when results-dir
        (let [src "target/test-results"
              dst (cond-> results-dir
                          connector-name (str "/" connector-name))]
          (doseq [input (file-seq (io/file src))]
            (when (.isFile input)
              (let [output (io/file (str dst "/" (.getName input)))]
                (log/infof "copying %s to %s" input output)
                (io/make-parents output)
                (io/copy input output)))))))))
