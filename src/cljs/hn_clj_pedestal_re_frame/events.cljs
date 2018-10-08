(ns hn-clj-pedestal-re-frame.events
  (:require
   [re-frame.core :as re-frame]
   [re-graph.core :as re-graph]
   [hn-clj-pedestal-re-frame.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

(re-frame/reg-event-db
  ::on-feed
  (fn [db [_ {:keys [data errors] :as payload}]]
    (update db :links concat (:feed data))))

(re-frame/reg-event-db
  ::initialize-db
  (fn-traced [db  [_ _]]
    (re-frame/dispatch
      [::re-graph/query
       "{ feed { id url description } }"
       {}
       [::on-feed]])
    db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))
