(def +version+ "3.9-SNAPSHOT")

(set-env!
  :project 'com.sixsq.slipstream/SlipStreamTests-clj
  :version +version+
  :license {"Apache 2.0" "http://www.apache.org/licenses/LICENSE-2.0.txt"}
  :edition "community"

  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [sixsq/build-utils "0.1.3" :scope "test"]])

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

                   [com.cemerick/url "0.1.1" :scope "test"]
                   [com.sixsq.slipstream/SlipStreamClientAPI-jar :version :scope "test"]
                   
                   [adzerk/boot-test]
                   [adzerk/boot-reload]
                   [tolitius/boot-check]]))))

(set-env!
  :source-paths #{"test" "resources"})

(require
  '[adzerk.boot-test :refer [test]]
  '[adzerk.boot-reload :refer [reload]]
  '[tolitius.boot-check :refer [with-yagni with-eastwood with-kibit with-bikeshed]])

(task-options!
  pom {:project (get-env :project)
       :version (get-env :version)}
  test {:exclusions '#{sixsq.slipstream.test-base}
        :junit-output-to ""})
