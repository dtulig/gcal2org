(ns user
  (:require [environ.core :refer [env]]
            [gcal2org.core :as core]))

(defn google-cal-creds []
  {:client-id (env :client-id)
   :client-secret (env :client-secret)})

(defn build-client []
  (core/build-client "/tmp/gcal2org.store" "./client_secrets.json"))
