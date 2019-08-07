(ns hn-clj-pedestal-re-frame.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [re-frame.core :as re-frame]
   [hn-clj-pedestal-re-frame.config :as config]))


;-----------------------------------------------------------------------
; Core
;-----------------------------------------------------------------------

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 :loading?
 (fn [db]
   (:loading? db)))

(re-frame/reg-sub
 :error?
 (fn [db]
   (:error db)))

(re-frame/reg-sub
 :headers
 (fn [db _]
   (get-in db config/token-header-path)))

(re-frame/reg-sub
 :auth?
 (fn [_]
   (re-frame/subscribe [:headers]))
 (fn [headers]
   (not (nil? headers))))


;-----------------------------------------------------------------------
; Lists
;-----------------------------------------------------------------------

(re-frame/reg-sub
 ::new-links
 (fn [db]
   (:new-links db)))

(re-frame/reg-sub
 :count
 (fn [db]
   (:count db)))

(re-frame/reg-sub
 ::top-links
 (fn [db]
   (:top-links db)))

(re-frame/reg-sub
 ::search-links
 (fn [db]
   (:search-links db)))
