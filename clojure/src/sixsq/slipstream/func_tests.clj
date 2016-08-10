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
                         :connector-name connector)
       (btest/test :namespaces (:namespaces opts)
                   :exclusions (:exclusions opts)
                   :filters (:filters opts)
                   :requires (:requires opts)
                   :junit-output-to (:junit-output-to opts))
       (ft/func-test-post :results-dir (ft/results-loc (:results-dir opts) connector)
                          :connector-name connector
                          :junit-output-to (:junit-output-to opts)))))

(defn- gen-tasks
  [opts]
  (if (empty? (:connectors opts))
    (pre-test-post opts)
    (mapcat identity
      (for [c (:connectors opts)]
        (pre-test-post opts c)))))

(boot/deftask func-test
  "SlipStream functional tests.

  Before running boot test task, writes configuration file for the tests.

  After tests are run, the test results (XML) are copied to 'results-dir' (or
  current directory).  If 'connector' is given, then test results are copied
  to the directory under 'connector' sub-directory in 'results-dir'.

  For connector specific tests (i.e. when 'connector' is provided) the
  metadata of the test suites in the test result files are renamed by
  prepending 'connector' to the 'package' and 'classname' attributes.
  "
  [; func-test-pre
   s endpoint ENDPOINT str "SlipStream endpoint"
   u username USERNAME str "SlipStream username"
   p password PASSWORD str "SlipStream password"
   _ app-uri APPURI str "Application URI (for deploying app and scaling)"
   _ comp-name COMPNAME str "Component name (for scalable tests)"
   _ comp-uri COMPURI str "Component URI (for deploying component)"
   c connectors CONNECTORS #{str} "Set of connector names"
   ; boot/test
   n namespaces NAMESPACE #{sym} "Set of namespace symbols of tests to run"
   e exclusions NAMESPACE #{sym} "Set of namespace symbols to exclude from tests"
   f filters    EXPR      #{edn} "Set of expressions that filter namespaces"
   r requires   REQUIRES  #{sym} "Extra namespaces to pre-load into the pool of test pods for speed"
   j junit-output-to JUNITOUT str "Output directory for junit formatted reports for each namespace"
   ; func-test-post
   d results-dir RESULTSDIR str "Output directory for test results"]
   (apply comp (gen-tasks *opts*)))

