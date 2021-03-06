(ns gcal2org.org
  (:import org.joda.time.Days)
  (:require [clj-time.format :as f]
            [clj-time.core :as t]
            [clojure.java.io :as io]))

(def org-mode-timestamp "yyyy-MM-dd EE HH:mm")
(def org-mode-time-only "HH:mm")
(def org-mode-date-only (f/formatter "yyyy-MM-dd"))

(def org-header "#+TITLE: Google Calendar Entries
#+AUTHOR: David Tulig
#+DESCRIPTION: Created using dtulig/gcal2org
#+CATEGORY: %s
#+STARTUP: hidestars
#+STARTUP: overview
")

(defn create-file-header [category]
  (format org-header
          category))

(defn- event-date-to-org-mode-timestamp
  ([start]
   (f/unparse
    (f/with-zone
      (f/formatter org-mode-timestamp)
      (.getZone start))
    start))
  ([start end]
   (if-not (nil? end)
     (str (if (and (> (.getDays (Days/daysBetween start end)) 0)
                   (= (.getHourOfDay start) 0)
                   (= (.getHourOfDay end) 0))
            (f/unparse org-mode-date-only start)
            (str (f/unparse (f/with-zone
                              (f/formatter org-mode-timestamp)
                              (.getZone start))
                            start)
                 "-"
                 (f/unparse (f/with-zone
                              (f/formatter org-mode-time-only)
                              (.getZone end))
                            end))))
     (event-date-to-org-mode-timestamp start))))

(defn get-org-event-timestamp [evt]
  (event-date-to-org-mode-timestamp (:start evt) (:end evt)))

(defn create-org-event-entry [event]
  (let [title (:summary event)
        timestamp (get-org-event-timestamp event)
        description (:description event)]
    (when-not (empty? title)
      (format "* %s
<%s>
:PROPERTIES:
:END:

%s
"
              title
              timestamp
              description))))
