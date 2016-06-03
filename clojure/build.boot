(def +version+ "3.6-SNAPSHOT")

(defn sixsq-repo [version edition]
  (let [nexus-url "http://nexus.sixsq.com/content/repositories/"
        repo-type (if (re-find #"SNAPSHOT" version)
                    "snapshots"
                    "releases")]
    (str nexus-url repo-type "-" edition)))

(set-env!
  :project 'com.sixsq.slipstream/SlipStreamTests-clj
  :version +version+
  :license {"Apache 2.0" "http://www.apache.org/licenses/LICENSE-2.0.txt"}
  :edition "community")

(set-env!
  :source-paths #{"test" "resources"}

  :repositories
  #(reduce conj % [["sixsq" {:url (sixsq-repo (get-env :version) (get-env :edition))}]])

  :dependencies
  '[[org.clojure/clojure "1.8.0" :scope "provided"]
    [adzerk/boot-test "1.1.1" :scope "test"]
    [adzerk/boot-reload "0.4.8" :scope "test"]
    [tolitius/boot-check "0.1.1" :scope "test"]
    [sixsq/boot-deputil "0.2.2" :scope "test"]
    [com.cemerick/url "0.1.1" :scope "test"]
    [com.sixsq.slipstream/SlipStreamClientAPI-jar "3.6-SNAPSHOT" :scope "test"]])

(require
  '[adzerk.boot-test :refer [test]]
  '[adzerk.boot-reload :refer [reload]]
  '[sixsq.boot-deputil :refer [set-deps!]]
  '[tolitius.boot-check :refer [with-yagni with-eastwood with-kibit with-bikeshed]])

(task-options!
  pom {:project (get-env :project)
       :version (get-env :version)}
  checkout {:dependencies [['sixsq/default-deps (get-env :version)]]}
  test {:exclusions '#{sixsq.slipstream.test-base}
        :junit-output-to ""})

