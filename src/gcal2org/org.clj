(ns gcal2org.org
  (:import org.joda.time.Days)
  (:require [clj-time.format :as f]
            [clj-time.core :as t]))

(def org-mode-timestamp (f/with-zone (f/formatter "yyyy-MM-dd EE HH:mm") (t/default-time-zone)))
(def org-mode-time-only (f/with-zone (f/formatter "HH:mm") (t/default-time-zone)))
(def org-mode-date-only (f/formatter "yyyy-MM-dd"))

(defn create-file-header [category]
  (format "#+TITLE: Google Calendar Entries
#+AUTHOR: David Tulig
#+DESCRIPTION: Created using dtulig/gcal2org
#+CATEGORY: %s
#+STARTUP: hidestars
#+STARTUP: overview
" category))

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
