(def +version+ "3.64-SNAPSHOT")

(defproject com.sixsq.slipstream/SlipStreamTests-clj "3.64-SNAPSHOT"

  :description "SlipStream functional tests"

  :url "https://github.com/slipstream/SlipStreamTests"

  :license {:name         "Apache 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}

  :plugins [[lein-parent "0.3.2"]
            [test2junit "1.4.0"]]

  :parent-project {:coords  [sixsq/slipstream-parent "5.3.13"]
                   :inherit [:plugins
                             :min-lein-version
                             :managed-dependencies
                             :repositories
                             :deploy-repositories]}

  :source-paths ["src" "test"]

  :resource-paths ["resources"]

  :test2junit-output-dir "target/test-results"

  :pom-location "target/"

  :aot [sixsq.slipstream.prepare]

  :dependencies
  [[org.clojure/clojure]
   [org.clojure/clojurescript]
   [org.clojure/tools.cli]
   [com.cemerick/url]
   [com.sixsq.slipstream/SlipStreamClojureAPI-cimi ~+version+]])
