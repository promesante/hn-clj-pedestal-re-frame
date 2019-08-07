(ns hn-clj-pedestal-re-frame.events.graph-ql.queries
  (:require
   [re-frame.core :as re-frame]
   [re-graph.core :as re-graph]
   [hn-clj-pedestal-re-frame.db :as db]
   [hn-clj-pedestal-re-frame.config :as config]
   [hn-clj-pedestal-re-frame.graph-ql.queries :as queries]))


;-------------------------------------------------------------------------
; New Links
;-------------------------------------------------------------------------

(re-frame/reg-event-fx
 :fetch-new-links
 (fn [{:keys [db]} [_ page]]
   (let [first config/new-links-per-page
         skip (* config/new-links-per-page (- page 1))]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/query
                 queries/feed
                  {:first first :skip skip}
                 [:on-feed-new]]})))

(re-frame/reg-event-fx
 :on-feed-new
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [links (get-in data [:feed :links])
         count (get-in data [:feed :count])]
     {:db (-> db
              (assoc :loading? false)
              (assoc :new-links links)
              (assoc :count count))
      :dispatch [:set-active-panel :new-panel]})))


;-------------------------------------------------------------------------
; Top Links
;-------------------------------------------------------------------------

(re-frame/reg-event-fx
 :fetch-top-links
 (fn [{:keys [db]} [_ _]]
   (let [first config/top-links-per-page
         skip 1]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/query
                 queries/feed
                  {:first first :skip skip}
                 [:on-feed-top]]})))

(re-frame/reg-event-fx
 :on-feed-top
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [links (get-in data [:feed :links])
         votes (map (fn [link] (count (:votes link))) links)
         links-with-votes (filter (fn [link] (not (empty? (:votes link)))) links)
         ranked-links (sort
                       #(compare (count (:votes %2)) (count (:votes %1)))
                       links-with-votes)]
     {:db (-> db
              (assoc :loading? false)
              (assoc :top-links ranked-links))
      :dispatch [:set-active-panel :top-panel]})))


;-------------------------------------------------------------------------
; Search Links
;-------------------------------------------------------------------------

(re-frame/reg-event-fx
 :search-links
 (fn [{:keys [db]} [_ filter]]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/query
                 queries/search
                 {:filter filter}
                 [:on-search-links]]}))

(re-frame/reg-event-db
  :on-search-links
  (fn [db [_ {:keys [data errors] :as payload}]]
    (let [links (get-in data [:feed :links])]
      (-> db
          (assoc :loading? false)
          (assoc :search-links links)))))
