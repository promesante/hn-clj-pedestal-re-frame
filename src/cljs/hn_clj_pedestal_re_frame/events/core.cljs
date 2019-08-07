(ns hn-clj-pedestal-re-frame.events.core
  (:require
   [re-frame.core :as re-frame]
   [hodgepodge.core :refer [local-storage get-item set-item remove-item]]
   [hn-clj-pedestal-re-frame.routes :as routes]
   [hn-clj-pedestal-re-frame.db :as db]))


(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-fx
 :set-history
 (fn [route]
   (routes/set-history! route)))

(re-frame/reg-event-fx
 :navigate
 (fn [_ [_ route]]
   {:set-history route}))

(re-frame/reg-fx
 :set-local-store
 (fn [[key value]]
   (set-item local-storage key value)))

(re-frame/reg-fx
 :remove-local-store
 (fn [key]
   (remove-item local-storage key)))

(re-frame/reg-cofx
 :local-store
 (fn [coeffects key]
   (get-item local-storage key "empty")))
