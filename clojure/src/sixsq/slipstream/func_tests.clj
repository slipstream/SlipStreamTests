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
(ns sixsq.slipstream.func-tests
  (:require [boot.core :as boot]
            [clojure.string :as s]
            [sixsq.slipstream.func-tests-impl :as ft]
            [adzerk.boot-test :as btest]))

(defn- pre-test-post
  ([opts]
    (pre-test-post opts nil))
  ([opts connector]
    (conj []
       (ft/func-test-pre :endpoint (:endpoint opts)
                         :username (:username opts)
                         :password (:password opts)
                         :app-uri (:app-uri opts)
                         :comp-name (:comp-name opts)
                         :comp-uri (:comp-uri opts)
                         :insecure (:insecure opts)
                         :connector-name connector)
       (btest/test :namespaces (:namespaces opts)
                   :exclusions (:exclusions opts)
                   :filters (:filters opts)
                   :requires (:requires opts)
                   :junit-output-to (:junit-output-to opts))
       (ft/func-test-post :results-dir (:results-dir opts)
                          :junit-output-to (:junit-output-to opts)
                          :connector-name connector))))

(defn- gen-tasks
  [opts]
  (if (empty? (:connectors opts))
    (pre-test-post opts)
    (mapcat identity
      (for [c (:connectors opts)]
        (pre-test-post opts c)))))

(defn- check-opts
  [opts usage]
  (if (and (> (count (:connectors opts)) 1) (s/blank? (:results-dir opts)))
    (do
      (boot.util/fail "\nProvide --results-dir to store results from multiple connectors.\n" (usage))
      (System/exit 1))))

(boot/deftask func-test
  "SlipStream functional tests.

  Before running boot test task, writes configuration file for the tests.

  After tests are run, the test results (XML) are copied to 'results-dir' (if
  provided).  If 'connectors' is given, then test results are copied
  to the directory under 'connectors' sub-directory in 'results-dir' (if
  provided).  Otherwise, the test results can be found under target directory.

  If 'connectors' is given multiple times, 'results-dir' must be
  provided as well.

  For connector specific tests (i.e. when 'connectors' is provided) the
  metadata of the test suites in the test result files are renamed by
  prepending the connector name to the 'package' and 'classname' attributes.
  "
  [; func-test-pre
   s endpoint ENDPOINT str "SlipStream endpoint"
   u username USERNAME str "SlipStream username"
   p password PASSWORD str "SlipStream password"
   _ app-uri APPURI str "Application URI (for deploying app and scaling)"
   _ comp-name COMPNAME str "Component name (for scalable tests)"
   _ comp-uri COMPURI str "Component URI (for deploying component)"
   c connectors CONNECTORS #{str} "Set of connector names. Provide --results-dir if more than one -c is given."
   i insecure bool "Insecure connection to SlipStream"
   ; boot/test
   n namespaces NAMESPACE #{sym} "Set of namespace symbols of tests to run"
   e exclusions NAMESPACE #{sym} "Set of namespace symbols to exclude from tests"
   f filters    EXPR      #{edn} "Set of expressions that filter namespaces"
   r requires   REQUIRES  #{sym} "Extra namespaces to pre-load into the pool of test pods for speed"
   j junit-output-to JUNITOUT str "Output directory for junit formatted reports for each namespace"
   ; func-test-post
   d results-dir RESULTSDIR str "Output directory for test results. Required if more than one -c is provided."]
  (check-opts *opts* *usage*)
  (apply comp (gen-tasks *opts*)))

