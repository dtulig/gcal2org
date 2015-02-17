(ns gcal2org.calendar
  (:import com.google.api.services.calendar.model.EventDateTime
           com.google.api.services.calendar.model.CalendarList
           com.google.api.services.calendar.Calendar$Builder
           com.google.api.services.calendar.model.Event
           com.google.api.client.util.DateTime
           org.joda.time.format.ISODateTimeFormat)
  (:require [clj-time.coerce :as c]
            [clj-time.core :as t]
            [clj-time.format :as f]))

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

(defn joda-to-date-time [joda-time]
  (DateTime. (.toDate joda-time)))

(defn get-calendar-events [client calendar-id min-time max-time next-page-token]
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
      (f/parse (ISODateTimeFormat/dateTime) (.toString (.getDateTime ts))))))

(defn event-to-map [event]
  {:recurring-id (.getRecurringEventId event)
   :summary (or (.getSummary event) "")
   :description (or (.getDescription event) "")
   :start (parse-event-date-time (.getStart event))
   :end (parse-event-date-time (.getEnd event))})

(defn parse-calendar-events
  "Turns a list of calendar events into clojure maps."
  [events]
  (let [items (.getItems events)]
    (map event-to-map items)))
