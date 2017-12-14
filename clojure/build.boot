(def +version+ "3.42-SNAPSHOT")

(set-env!
  :project 'com.sixsq.slipstream/SlipStreamTests-clj
  :version +version+
  :license {"Apache 2.0" "http://www.apache.org/licenses/LICENSE-2.0.txt"}
  :edition "community"

  :dependencies '[[org.clojure/clojure "1.9.0"]
                  [sixsq/build-utils "0.1.4" :scope "test"]])

(require '[sixsq.build-fns :refer [merge-defaults
                                   sixsq-nexus-url
                                   lein-generate]])

(set-env!
  :repositories
  #(reduce conj % [["sixsq" {:url (sixsq-nexus-url)}]])

  :dependencies
  #(vec (concat %
                (merge-defaults
                 ['sixsq/default-deps (get-env :version)]
                 '[[org.clojure/clojure]
                   [org.clojure/clojurescript]

                   [clj-http "2.2.0"]
                   [clj-jwt "0.1.1"]
                   [com.cemerick/url nil :scope "test"]
                   [com.sixsq.slipstream/SlipStreamClientAPI-jar :version :scope "test"]

                   [adzerk/boot-test]
                   [adzerk/boot-reload]
                   [tolitius/boot-check]]))))

(set-env!
  :resource-paths #{"src"}
  :source-paths #{"test" "resources"})

(require
  '[adzerk.boot-test :refer [test]]
  '[sixsq.slipstream.func-tests :refer [func-test]]
  '[adzerk.boot-reload :refer [reload]]
  '[tolitius.boot-check :refer [with-yagni with-eastwood with-kibit with-bikeshed]])

(def test-opts {:exclusions '#{sixsq.slipstream.test-base}
                :junit-output-to ""})

(task-options!
  pom {:project (get-env :project)
       :version (get-env :version)}
  test test-opts
  func-test test-opts)
