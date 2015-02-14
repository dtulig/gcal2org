(ns gcal2org.cli
  (:require [clojure.tools.cli :refer [parse-opts]]
            [environ.core :refer [env]]))

(defn cli-options []
  [["-c" "--calendar CALENDAR" "Calendar id (typically your gmail address)"]
   ["-d" "--data FILEPATH" "Where data such as the credentials are stored."
    :default (str (env :home) "/.gcal2org")]
   ["-s" "--store STORE" "Credentials store"]
   ["-o" "--output FILEPATH" "The file to be written, otherwise stdout"]
   [nil "--category CATEGORY" "Org mode CATEGORY."
    :default "google"]
   ["-h" "--help"]])

(defn get-cmd-line-args [args]
  (parse-opts args (cli-options)))
