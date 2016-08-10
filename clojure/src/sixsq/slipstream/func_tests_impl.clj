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
(ns sixsq.slipstream.func-tests-impl
  (:require
    [boot.core :as boot]
    [clojure.java.io :as io]
    [clojure.string :as s]
    [clojure.pprint :refer [pprint]]
    [clojure.zip :as zip]
    [clojure.data.xml :as xml]))

(def test-conf-filename "test-config.edn")

(defn get-file-path
  [fileset fname]
  (try
    (-> (-> fileset
          (boot/tmp-get fname)
          boot/tmp-dir)
      (io/file fname)
      .getPath)
    (catch IllegalArgumentException e)))

(defn write-config
  [content fname fileset]
  (if-let [f (get-file-path fileset fname)]
    (spit f (with-out-str (pr content)))
    (throw
      (ex-info
        (str "ERROR: failed to find file " fname " in boot's fileset.") {}))))

(defn results-loc
  [results-dir connector]
  (let [rd (or results-dir ".")]
    (if-not (s/blank? connector)
      (str (io/file rd connector))
      rd)))

(defn xml-res-re
  [junit-output-to]
  (re-pattern (format "^%s.*\\.xml$" junit-output-to)))

(defn is-xml-res-file
  [f junit-output-to]
  (re-find (xml-res-re junit-output-to) (:path f)))

(defn find-res-xmls
  [fileset junit-output-to]
  (for [f (boot/output-files fileset) :when (is-xml-res-file f junit-output-to)]
    (str (io/file (:dir f) (:path f)))))

(defn copy-file [source-path dest-path]
  (io/copy (io/file source-path) (io/file dest-path)))

(defn mkdirs
  [path]
  (.mkdirs (io/file path)))

(defn tag-matcher
  [loc tagname]
  (let [tag (:tag (zip/node loc))]
    (= (keyword tagname) tag)))

(defn match-testsuite?
  [loc]
  (tag-matcher loc "testsuite"))

(defn match-testcase?
  [loc]
  (tag-matcher loc "testcase"))

(defn attr-editor
  [attr-name prep-val node]
  (let [an (keyword attr-name)
        package-name (-> node :attrs an)
        new-content (assoc (:attrs node) an (format "%s.%s" prep-val package-name))]
    (assoc node :attrs new-content)))

(defn package-editor
  [prep-val node]
  (attr-editor "package" prep-val node))

(defn classname-editor
  [prep-val node]
  (attr-editor "classname" prep-val node))

(defn tree-edit
  "Take a zipper, a function that matches a pattern in the tree,
   and a function that edits the current location in the tree.  Examine the tree
   nodes in depth-first order, determine whether the matcher matches, and if so
   apply the editor."
  [zipper matcher editor]
  (loop [loc zipper]
    (if (zip/end? loc)
      (zip/root loc)
      (if (matcher loc)
        (let [new-loc (zip/edit loc editor)]
          (if (not (= (zip/node new-loc) (zip/node loc)))
            (recur (zip/next new-loc))))
        (recur (zip/next loc))))))

(defn update-test-meta-xml-str
  [xml-str cname]
  (-> xml-str
    xml/parse-str
    zip/xml-zip
    (tree-edit match-testsuite? (partial package-editor cname))
    zip/xml-zip
    (tree-edit match-testcase? (partial classname-editor cname))
    xml/indent-str))

(defn update-test-meta
  [filename cname]
  (let [s (-> filename
              slurp
              (update-test-meta-xml-str cname))]
    (spit filename s)))

(defn test-file-copy-update
  [srcfn results-dir connector-name]
  (let [dstfn (str (io/file results-dir (.getName (io/file srcfn))))]
    (copy-file srcfn dstfn)
    (if-not (empty? connector-name)
      (update-test-meta dstfn connector-name))))

(boot/deftask func-test-pre
  "Run before SlipStream functional tests."
  [_ serviceurl SERVICEURL str "SlipStream endpoint"
   _ username USERNAME str "SlipStream username"
   _ password PASSWORD str "SlipStream password"
   _ app-uri APPURI str "Application URI (for deploying app and scaling)"
   _ comp-name COMPNAME str "Component name (for scalable tests)"
   _ comp-uri COMPURI str "Component URI (for deploying component)"
   _ insecure? bool "Insecure connection to SlipStream"
   _ connector-name CONNECTOR str "Connector name"]
  (fn middleware [next-task]
      (fn handler [fileset]
        (write-config *opts* test-conf-filename fileset)
        (next-task fileset))))

(boot/deftask func-test-post
  "Run after SlipStream functional tests."
  [_ results-dir RESULTSDIR str "Output directory for test results"
   _ connector-name CONNECTOR str "Connector name"
   _ junit-output-to JUNITOUT str "Output directory for junit formatted reports for each namespace"]
  (fn middleware [next-task]
    (fn handler [fileset]
      (if-not (s/blank? results-dir)
        (do
          (mkdirs results-dir)
          (doseq [fn (find-res-xmls fileset junit-output-to)]
            (test-file-copy-update fn results-dir connector-name))))
      (next-task fileset))))

