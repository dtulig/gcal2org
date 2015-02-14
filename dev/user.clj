(ns user
  (:require [environ.core :refer [env]]
            [gcal2org.core :as core]
            [gcal2org.client :as client]))

(defn google-cal-creds []
  {:client-id (env :client-id)
   :client-secret (env :client-secret)})

(defn build-client []
  (client/build-client "/tmp/gcal2org.store" (str (env :home) "/.gcal2org/client_secrets.json")))
