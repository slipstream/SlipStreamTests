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

(boot/deftask func-test
  "SlipStream functional tests.

  Before running boot test task, writes configuration file for the tests.

  After tests are ran, the test results (XML) are copied to 'results-dir' (or
  current directory).  If 'connector' is give, the test results are copied to
  the directory under 'connector' sub-directory in 'results-dir'.

  For connector specific tests (i.e. when 'connector' is provided) the metadata 
  of the test suites in the test result files are renamed by prepending 'connector'
  to the 'package' and 'classname' attributes.
  "
  [; func-test-pre
   s endpoint ENDPOINT str "SlipStream endpoint."
   u username USERNAME str "SlipStream username."
   p password PASSWORD str "SlipStream password."
   _ app-uri APPURI str "Application URI (for deploying app and scaling)."
   _ comp-name COMPNAME str "Component name (for scalable tests)."
   _ comp-uri COMPURI str "Component URI (for deploying component)."
   c connectors CONNECTORS #{str} "Set of connector names."
   ; boot/test
   n namespaces NAMESPACE #{sym} "The set of namespace symbols to run tests in."
   e exclusions NAMESPACE #{sym} "The set of namespace symbols to be excluded from test."
   f filters    EXPR      #{edn} "The set of expressions to use to filter namespaces."
   r requires   REQUIRES  #{sym} "Extra namespaces to pre-load into the pool of test pods for speed."
   j junit-output-to JUNITOUT str "The directory where a junit formatted report will be generated for each ns"
   ; func-test-post
   d results-dir RESULTSDIR str "Directory where to copy the test results to."]
  (let [tasks (mapcat identity
                (for [c connectors]
                  (conj []
                        (ft/func-test-pre :serviceurl endpoint
                                          :username username
                                          :password password
                                          :app-uri app-uri
                                          :comp-name comp-name
                                          :comp-uri comp-uri
                                          :connector-name c)
                        (btest/test :namespaces namespaces
                                    :exclusions exclusions
                                    :filters filters
                                    :requires requires
                                    :junit-output-to junit-output-to)
                        (ft/func-test-post :results-dir (ft/results-loc results-dir c)
                                           :connector-name c
                                           :junit-output-to junit-output-to))))]
    (apply comp tasks)))

