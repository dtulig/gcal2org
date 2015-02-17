(ns gcal2org.org
  (:import org.joda.time.Days)
  (:require [clj-time.format :as f]
            [clj-time.core :as t]
            [clojure.java.io :as io]))

(def org-mode-timestamp (f/formatter "yyyy-MM-dd EE HH:mm"))
(def org-mode-time-only (f/formatter "HH:mm"))
(def org-mode-date-only (f/formatter "yyyy-MM-dd"))

(defn create-file-header [category]
  (format (-> (io/resource "org-output/header.txt")
              io/file
              slurp)
          category))

(defn- event-date-to-org-mode-timestamp
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
