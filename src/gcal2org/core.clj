(ns gcal2org.core
  (:require [gcal2org.client :as client]
            [gcal2org.calendar :as calendar]
            [gcal2org.org :as org]
            [gcal2org.cli :as cli]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-time.local :as l]
            [clj-time.core :as t])
  (:gen-class))

(def max-time (t/plus (.withMillisOfDay (l/local-now) 0) (t/weeks 4)))
(def min-time (t/minus (.withMillisOfDay (l/local-now) 0) (t/weeks 4)))

(defn get-events [client calendar-id min-time max-time]
  (loop [result (calendar/get-calendar-events client calendar-id min-time max-time nil)
         results (calendar/parse-calendar-events result)]
    (println (.getNextPageToken result))
    (if (.getNextPageToken result)
      (let [page-result (calendar/get-calendar-events client
                                                      calendar-id
                                                      min-time
                                                      max-time
                                                      (.getNextPageToken result))]
        (recur page-result
               (into results
                     (calendar/parse-calendar-events page-result))))
      results)))

(defn create-org-output [category events]
  (str (org/create-file-header category)
       (str/join "\n" (map org/create-org-event-entry events))))

(defn -main [& args]
  (let [{:keys [options] :as cmd-args} (cli/get-cmd-line-args args)]
    (println cmd-args)
    (io/make-parents (:output options))
    (let [client (client/build-client (:store options) (str (:data options) "/client_secrets.json"))]
      (->> (get-events client (:calendar options) min-time max-time)
           (create-org-output (:category options))
           (spit (:output options))))))

