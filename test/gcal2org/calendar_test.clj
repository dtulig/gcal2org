(ns gcal2org.calendar-test
  (:import com.google.api.services.calendar.model.EventDateTime
           com.google.api.services.calendar.model.Event
           com.google.api.services.calendar.model.Event$Creator
           com.google.api.services.calendar.model.Events
           com.google.api.client.util.DateTime)
  (:require [clojure.test :refer :all]
            [gcal2org.calendar :refer :all]
            [clj-time.core :as t]))

(deftest parse-event-date-time-with-date-time
  (testing "Take a google api EventDateTime with a date and a time and
  turn it into a joda-time object. The result should be in UTC."
    (let [dt (t/date-time 2015 2 13 16 30 0 0)
          evdt (.. (EventDateTime.)
                   (setDateTime (DateTime. "2015-02-13T16:30:01-06:00")))
          parsed-evdt (parse-event-date-time evdt)]
      (is (= 13 (t/day parsed-evdt)))
      (is (= 22 (t/hour parsed-evdt))))))

(deftest parse-event-date-time-with-date-only
  (testing "Take a google api EventDateTime with only a date and turn
  it into a joda-time object. The parsed date is in UTC but that
  should be the final representation."
    (let [dt (t/date-time 2015 2 13 16 30 0 0)
          evdt (.. (EventDateTime.)
                   (setDate (DateTime. "2015-02-13")))
          parsed-evdt (parse-event-date-time evdt)]
      (is (= 13))
      (is (= 0 (t/hour parsed-evdt))))))

(def full-day-event (.. (Event.)
                        (setCreated (DateTime. "2012-01-14T19:57:10.000Z"))
                        (setCreator (.. (Event$Creator.)
                                        (setDisplayName "John Doe")
                                        (setEmail "example@example.com")
                                        (setSelf true)))
                        (setEtag "\"2653142060000000\"")
                        (setHtmlLink "https://www.google.com/calendar/event?eid=randomeid")
                        (setICalUID "fake@google.com")
                        (setId "fake_20150218")
                        (setKind "calendar#event")
                        (setOriginalStartTime (.. (EventDateTime.)
                                                  (setDateTime
                                                   (DateTime. "2015-02-18"))))
                        (setStart (.. (EventDateTime.)
                                      (setDateTime
                                       (DateTime. "2015-02-18"))))
                        (setEnd (.. (EventDateTime.)
                                    (setDateTime
                                     (DateTime. "2015-02-19"))))
                        (setRecurringEventId "fakerecurringid")
                        (setSummary "Birthday")))

(deftest event-to-map-full-day
  (testing "Take a full day event and parse it into a clojure
  representation."
    (let [parsed-evt (event-to-map full-day-event)]
      (is (= "Birthday" (:summary parsed-evt)))
      (is (= "2015-02-18T00:00:00.000Z" (.toString (:start parsed-evt))))
      (is (= "2015-02-19T00:00:00.000Z" (.toString (:end parsed-evt)))))))

(deftest parse-calendar-events-single
  (testing "Take a calendar event list and turn parse it into a
  clojure list."
    (let [events (.. (Events.)
                     (setItems (java.util.Collections/singletonList full-day-event)))
          parsed-events (parse-calendar-events events)]
      (is (= 1 (count parsed-events)))
      (is (= "Birthday" (:summary (first parsed-events)))))))
