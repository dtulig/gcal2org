(ns gcal2org.calendar-test
  (:import com.google.api.services.calendar.model.EventDateTime
           com.google.api.client.util.DateTime)
  (:require [clojure.test :refer :all]
            [gcal2org.calendar :refer :all]
            [clj-time.core :as t]))

(deftest parse-event-date-time-with-date-time
  (testing "Take a google api EventDateTime with a date and a time and
  turn it into a joda-time object."
    (let [dt (t/date-time 2015 2 13 16 30 0 0)
          evdt (.. (EventDateTime.)
                   (setDateTime (DateTime. (.toDate dt))))
          parsed-evdt (parse-event-date-time evdt)]
      (is (= (t/day dt) (t/day parsed-evdt)))
      (is (= (t/hour dt) (t/hour parsed-evdt))))))

(deftest parse-event-date-time-with-date-only
  (testing "Take a google api EventDateTime with only a date and turn
  it into a joda-time object."
    (let [dt (t/date-time 2015 2 13 16 30 0 0)
          evdt (.. (EventDateTime.)
                   (setDate (DateTime. (.toDate dt))))
          parsed-evdt (parse-event-date-time evdt)]
      (is (= (t/day dt) (t/day parsed-evdt)))
      (is (= (t/hour dt) (t/hour parsed-evdt))))))
