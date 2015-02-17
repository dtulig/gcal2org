(ns gcal2org.org-test
  (:require [clojure.test :refer :all]
            [gcal2org.org :refer :all]
            [clojure.string :as str]
            [clj-time.local :as l]
            [clj-time.core :as t]))

(deftest create-file-header-ensure-single-newline
  (testing "Make sure there is a single newline after the header
  renders."
    (let [result (->> (create-file-header "random-category")
                      reverse
                      (take 2))]
      (is (= \newline (first result)))
      (is (not (= \newline (second result)))))))

(deftest create-file-header-ensure-category
  (testing "Make sure the category has been substituded property."
    (let [result (create-file-header "find-me")]
      (is (not (= -1 (.indexOf result "#+CATEGORY: find-me")))))))

(deftest create-org-event-entry-newlines
  (testing "No newlines at the start and one at the end of the entry."
    (let [result (create-org-event-entry {:summary "Birthday"
                                          :start (l/local-now)
                                          :end (t/plus (l/local-now) (t/hours 1))
                                          :description "Desc"})
          last-two (take 2 (reverse result))]
      (is (not (= \newline (first result))))
      (is (= \newline (first last-two)))
      (is (not (= \newline (second last-two)))))))

(deftest create-org-event-entry-summary
  (testing "First line should be the summary."
    (let [result (create-org-event-entry {:summary "Birthday"
                                          :start (l/local-now)
                                          :end (t/plus (l/local-now) (t/hours 1))
                                          :description "Desc"})]
      (is (= "* Birthday" (first (str/split result #"\n")))))))

(deftest get-org-event-timestamp-single-timestamp-utc
  (testing "Simple timestamp to org format."
    (is (= "2015-02-15 Sun 23:31"
           (get-org-event-timestamp {:start (t/date-time 2015 2 15 23 31 1 0) :end nil})))))

(deftest get-org-event-timestamp-single-timestamp
  (testing "Simple timestamp to org format."
    (is (= "2015-02-15 Sun 17:31"
           (get-org-event-timestamp {:start (t/to-time-zone (t/date-time 2015 2 15 23 31 1 0)
                                                            (t/time-zone-for-offset -6))
                                     :end nil})))))

(deftest get-org-event-timestamp-start-end
  (testing "Simple timestamp to org format."
    (is (= "2015-02-15 Sun 16:31-17:31"
           (get-org-event-timestamp {:start (t/to-time-zone (t/date-time 2015 2 15 22 31 1 0)
                                                            (t/time-zone-for-offset -6))
                                     :end (t/to-time-zone (t/date-time 2015 2 15 23 31 1 0)
                                                          (t/time-zone-for-offset -6))})))))

(deftest get-org-event-timestamp-date-only
  (testing "Simple timestamp to org format."
    (is (= "2015-02-16"
           (get-org-event-timestamp {:start (t/date-time 2015 2 16 0 0 0 0)
                                     :end (t/date-time 2015 2 17 0 0 0 0)})))))
