(ns hn-clj-pedestal-re-frame.events.init
  (:require
   [re-frame.core :as re-frame]
   [re-graph.core :as re-graph]
   [hn-clj-pedestal-re-frame.db :as db]
   [hn-clj-pedestal-re-frame.graph-ql.subscriptions :as subscriptions]))


(re-frame/reg-event-fx
   ::init
   (fn [{:keys [db]} [_ _]]
     {:db db/default-db
      :dispatch [:init-re-graph]}))

(re-frame/reg-event-fx
 :init-re-graph
 (fn [{:keys [db]} [_ _]]
     {:db (-> db
              (assoc :loading? true)
              (assoc :error false))
      :dispatch-n (list
                   [::re-graph/init
                    {:http-parameters
                     {:with-credentials? false
                      :headers nil}}]
                   [::re-graph/subscribe
                    :subscribe-to-new-links
                    subscriptions/new-link
                    {}
                    [:on-new-link]]
                   [::re-graph/subscribe
                    :subscribe-to-new-votes
                    subscriptions/new-vote
                    {}
                    [:on-new-vote]])}))
