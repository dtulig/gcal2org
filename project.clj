(defproject gcal2org "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [clj-time "0.9.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 
                 ;; Google APIs
                 [com.google.apis/google-api-services-calendar "v3-rev115-1.19.1"]
                 [com.google.api-client/google-api-client "1.19.0"]
                 [com.google.http-client/google-http-client-jackson2 "1.19.0"]
                 [com.google.oauth-client/google-oauth-client-jetty "1.19.0"]]

  :source-paths ["src"]

  :main gcal2org.core
  
  :profiles {:dev-common {:source-paths ["dev"]
                          :dependencies [[spyscope "0.1.5"]]
                          :plugins [[cider/cider-nrepl "0.8.2"]
                                    [lein-environ "1.0.0"]
                                    [jonase/eastwood "0.2.1"]]
                          :injections [(require 'spyscope.core)]}
             :dev-env {}
             :dev [:dev-common :dev-env]
             :uberjar {:aot :all}}

  :eastwood {:add-linters [:unused-fn-args :unused-locals :unused-private-vars]})
