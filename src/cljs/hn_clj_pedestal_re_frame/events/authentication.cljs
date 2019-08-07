(ns hn-clj-pedestal-re-frame.events.authentication
  (:require
   [re-frame.core :as re-frame]
   [hn-clj-pedestal-re-frame.db :as db]
   [hn-clj-pedestal-re-frame.config :as config]))


(re-frame/reg-event-fx
 :logout
 (fn [{:keys [db]} _]
     {:remove-local-store ["token"]
      :db (-> db
              (assoc :loading? false)
              (assoc-in
                config/token-header-path
                nil))
      :dispatch [:navigate :home]}))
