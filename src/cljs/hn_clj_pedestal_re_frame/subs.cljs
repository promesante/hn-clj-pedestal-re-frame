(ns hn-clj-pedestal-re-frame.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::links
 (fn [db]
   (:links db)))

(re-frame/reg-sub
 ::search-links
 (fn [db]
   (:search-links db)))

(re-frame/reg-sub
 :loading?
 (fn [db]
   (:loading? db)))

(re-frame/reg-sub
 :error?
 (fn [db]
   (:error db)))

; (re-frame/register-sub
;  :loading?
;  (fn [db]
;    (reaction (:loading? @db))))

; (re-frame/register-sub
;  :error?
;  (fn [db]
;    (reaction (:error @db))))
