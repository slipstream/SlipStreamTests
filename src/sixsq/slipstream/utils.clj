(ns sixsq.slipstream.utils
  (:require
    [clojure.string :as str]
    [taoensso.timbre :as log]))


(defn exit
  ([]
   (exit 0 nil))
  ([status]
   (exit status nil))
  ([status msg]
   (when msg
     (if-not (zero? status)
       (log/error msg)
       (log/info msg)))
   (System/exit status)))

(defn success
  ([]
   (exit))
  ([msg]
   (exit 0 msg)))


(defn failure
  [& msg]
  (exit 1 (str/join msg)))


(defn error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))


(def cli-options
  [["-s" "--endpoint ENDPOINT" "SlipStream endpoint"]
   ["-u" "--username USERNAME" "SlipStream username"]
   ["-p" "--password PASSWORD" "SlipStream password"]
   [nil "--app-uri APP_URI" "application URI for deploying application and scaling"]
   [nil "--comp-name COMP_NAME" "component name for scalable tests"]
   [nil "--comp-uri COMP_URI" "component URI for deploying component"]
   ["-c" "--connectors CONNECTORS" "connector name"
    :id :connector-name]
   ["-i" "--insecure" "insecure connection to SlipStream"
    :id :insecure?]
   ["-d" "--results-dir RESULTS_DIR" "output directory for test results"]
   ["-h" "--help" "usage information"]
   ])
