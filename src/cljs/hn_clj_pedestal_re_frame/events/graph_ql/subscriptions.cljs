(ns hn-clj-pedestal-re-frame.events.graph-ql.subscriptions
  (:require
   [re-frame.core :as re-frame]
   [hn-clj-pedestal-re-frame.events.utils :as utils]
   [hn-clj-pedestal-re-frame.db :as db]))


(re-frame/reg-event-db
  :on-new-link
  (fn [db [_ {:keys [data errors] :as payload}]]
    (let [link-new (:newLink data)
          links-prev (:new-links db)
          created? (utils/created? link-new links-prev)]
      (if created?
        db
        (let [links (utils/add-link link-new links-prev)]
          (-> db
              (assoc :loading? false)
              (assoc :new-links links)))))))

(re-frame/reg-event-fx
 :on-new-vote
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [vote-new (:newVote data)]
     {:dispatch [:on-new-vote-db vote-new]})))
