(ns gcal2org.core
  (:import com.google.api.client.json.jackson2.JacksonFactory
           com.google.api.services.calendar.model.EventDateTime
           com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
           com.google.api.client.util.store.FileDataStoreFactory
           com.google.api.services.calendar.model.CalendarList
           com.google.api.services.calendar.Calendar$Builder
           com.google.api.services.calendar.model.Event
           com.google.api.client.util.DateTime
           org.joda.time.Days)
  (:require [gcal2org.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clojure.tools.cli :refer [parse-opts]]
            [environ.core :refer [env]])
  (:gen-class))

(def cli-options
  [["-c" "--calendar CALENDAR" "Calendar id (typically your gmail address)"]
   ["-d" "--data FILEPATH" "Where data such as the credentials are stored."
    :default (str (env :home) "/.gcal2org")]
   ["-s" "--store STORE" "Credentials store"]
   ["-o" "--output FILEPATH" "The file to be written, otherwise stdout"]
   [nil "--category CATEGORY" "Org mode CATEGORY."
    :default "google"]
   ["-h" "--help"]])

(defn show-calendars [client]
  (.. client
      calendarList
      list
      execute))

(defn get-calendar [client calendar-id]
  (.. client
      calendars
      (get calendar-id)
      execute))

(def max-time (t/plus (.withMillisOfDay (l/local-now) 0) (t/weeks 4)))
(def min-time (t/minus (.withMillisOfDay (l/local-now) 0) (t/weeks 4)))

(defn joda-to-date-time [joda-time]
  (DateTime. (.toDate joda-time)))

(defn get-calendar-events [client calendar-id start end next-page-token]
  (.. client
      events
      (list calendar-id)
      (setTimeMax (joda-to-date-time max-time))
      (setTimeMin (joda-to-date-time min-time))
      (setSingleEvents true)
      (setPageToken next-page-token)
      execute))

(defn parse-event-date-time [#^EventDateTime ts]
  (when ts
    (if (nil? (.getDateTime ts))
      (when-not (nil? (.getDate ts))
        (c/from-long (.getValue (.getDate ts))))
      (c/from-long (.getValue (.getDateTime ts))))))

(defn event-to-map [event]
  {:recurring-id (.getRecurringEventId event)
   :summary (or (.getSummary event) "")
   :description (or (.getDescription event) "")
   :date-time-start (.getStart event)
   :date-time-end (.getEnd event)
   :start (parse-event-date-time (.getStart event))
   :end (parse-event-date-time (.getEnd event))})

(defn parse-calendar-events
  "Turns a list of calendar events into clojure maps."
  [events]
  (let [items (.getItems events)]
    (map event-to-map items)))

(defn parse-calendar-items
  "Turns a list of calendar events into clojure maps."
  [items]
  (map event-to-map items))

(defn range-predicate [event min-time max-time]
  (let [interval (t/interval min-time max-time)]
    (or (t/within? interval (:start event))
        (t/within? interval (:end event)))))

(defn remove-events-out-of-range [events min-time max-time]
  (filter #(range-predicate % min-time max-time) events))

(defn get-recurring-events [client calendar-id recurring-id min-time max-time]
  (.. client
      events
      (instances calendar-id recurring-id)
      (setTimeMax (joda-to-date-time max-time))
      (setTimeMin (joda-to-date-time min-time))
      execute))

(defn expand-recurring-events [events client calendar-id min-time max-time]
  (map #(if (:recurring-id %)
          (-> (get-recurring-events client calendar-id (:recurring-id %) min-time max-time)
              parse-calendar-events)
          %)
       events))

(defn get-calendar-ids [#^CalendarList calendar-list]
  (map #(.getId %) (.getItems calendar-list)))

(defn create-file-header [category]
  (format "#+TITLE: Google Calendar Entries
#+AUTHOR: David Tulig
#+DESCRIPTION: Created using dtulig/gcal2org
#+CATEGORY: %s
#+STARTUP: hidestars
#+STARTUP: overview
" category))

(def org-mode-timestamp (f/with-zone (f/formatter "yyyy-MM-dd EE HH:mm") (t/default-time-zone)))
(def org-mode-time-only (f/with-zone (f/formatter "HH:mm") (t/default-time-zone)))
(def org-mode-date-only (f/formatter "yyyy-MM-dd"))

(defn event-date-to-org-mode-timestamp
  ([start]
   (f/unparse org-mode-timestamp start))
  ([start end]
   (if-not (nil? end)
     (str (if (and (> (.getDays (Days/daysBetween start end)) 0)
                   (= (.getHourOfDay start) 0)
                   (= (.getHourOfDay end) 0))
            (f/unparse org-mode-date-only start)
            (str (f/unparse org-mode-timestamp start)
                 "-"
                 (f/unparse org-mode-time-only end))))
     (event-date-to-org-mode-timestamp start))))

(defn get-org-event-timestamp [evt]
  (event-date-to-org-mode-timestamp (:start evt) (:end evt)))

(defn create-org-event-entry [event]
  (let [start (:date-time-start event)
        end (:date-time-end event)]
    (when-not (empty? (:summary event))
      (format "
* %s
<%s>
:PROPERTIES:
:END:

%s"
              (:summary event)
              (get-org-event-timestamp event)
              (:description event)))))

(defn build-client [store credentials-file]
  (let [http-transport (GoogleNetHttpTransport/newTrustedTransport)
        jackson-factory (JacksonFactory/getDefaultInstance)
        data-store-factory (FileDataStoreFactory. (io/file store))
        credential (client/authorize http-transport jackson-factory data-store-factory credentials-file)]
    (.. (com.google.api.services.calendar.Calendar$Builder.
                    http-transport jackson-factory credential)
                   (setApplicationName "")
                   (build))))

(defn get-events [client calendar-id min-time max-time]
  (loop [result (get-calendar-events client calendar-id min-time max-time nil)
         results (parse-calendar-events result)]
    (println (.getNextPageToken result))
    (if (.getNextPageToken result)
      (let [page-result (get-calendar-events client
                                             calendar-id
                                             min-time
                                             max-time
                                             (.getNextPageToken result))]
        (recur page-result
               (into results
                     (parse-calendar-events page-result))))
      results)))

(defn create-org-output [category events]
  (str (create-file-header category)
       (str/join "\n" (map create-org-event-entry events))))

(defn get-cmd-line-args [args]
  (parse-opts args cli-options))

(defn -main [& args]
  (let [{:keys [options] :as cmd-args} (get-cmd-line-args args)]
    (println cmd-args)
    (io/make-parents (:output options))
    (let [client (build-client (:store options) (str (:data options) "/client_secrets.json"))]
      (->> (get-events client (:calendar options) min-time max-time)
           (create-org-output (:category options))
           (spit (:output options))))))

