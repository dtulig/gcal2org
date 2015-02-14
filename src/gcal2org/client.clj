(ns gcal2org.client
  (:import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
           com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
           com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow$Builder
           com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
           com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
           com.google.api.client.json.jackson2.JacksonFactory
           com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
           com.google.api.client.util.store.FileDataStoreFactory
           com.google.api.services.calendar.CalendarScopes
           com.google.api.client.auth.oauth2.Credential
           java.io.InputStreamReader)
  (:require [clojure.java.io :as io]))

(defn load-client-secrets [jackson-factory credentials-file]
  (GoogleClientSecrets/load
   jackson-factory
   (InputStreamReader. (-> credentials-file
                           io/file
                           io/input-stream))))

(defn #^Credential authorize
  [http-transport jackson-factory data-store-factory credentials-file]
  (let [client-secrets (load-client-secrets jackson-factory credentials-file)
        flow (.. (GoogleAuthorizationCodeFlow$Builder.
                  http-transport jackson-factory client-secrets (java.util.Collections/singleton (CalendarScopes/CALENDAR_READONLY)))
                 (setDataStoreFactory data-store-factory)
                 (build))]
    (.. (AuthorizationCodeInstalledApp. flow (LocalServerReceiver.))
        (authorize "user"))))

(defn build-client [store credentials-file]
  (let [http-transport (GoogleNetHttpTransport/newTrustedTransport)
        jackson-factory (JacksonFactory/getDefaultInstance)
        data-store-factory (FileDataStoreFactory. (io/file store))
        credential (authorize http-transport jackson-factory data-store-factory credentials-file)]
    (.. (com.google.api.services.calendar.Calendar$Builder.
         http-transport jackson-factory credential)
        (setApplicationName "gcal2org")
        (build))))
