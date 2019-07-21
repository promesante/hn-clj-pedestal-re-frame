(ns hn-clj-pedestal-re-frame.events
  (:require
   [re-frame.core :as re-frame]
   [re-graph.core :as re-graph]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [hodgepodge.core :refer [local-storage set-item remove-item]]
   [cljs-time.format :as format]
   [hn-clj-pedestal-re-frame.db :as db]
   [hn-clj-pedestal-re-frame.graph-ql.queries :as queries]
   [hn-clj-pedestal-re-frame.graph-ql.mutations :as mutations]
   [hn-clj-pedestal-re-frame.graph-ql.subscriptions :as subscriptions]))

(def new-links-per-page 5)
(def top-links-per-page 100)

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-fx
   ::init
   (fn [{:keys [db]} [_ _]]
     {:db db/default-db
      :dispatch [::init-re-graph]}))

(re-frame/reg-event-fx
 ::init-re-graph
 (fn [{:keys [db]} [_ _]]
     {:db (-> db
              (assoc :loading? true)
              (assoc :error false))
      :dispatch-n (list
                   [::re-graph/init {}]
                   [::re-graph/subscribe
                    :subscribe-to-new-links
                    subscriptions/new-link
                    {}
                    [::on-new-link]]
                   [::re-graph/subscribe
                    :subscribe-to-new-votes
                    subscriptions/new-vote
                    {}
                    [::on-new-vote]])}))
       
(re-frame/reg-event-db
  ::on-new-link
  (fn [db [_ {:keys [data errors] :as payload}]]
    (let [link-new (:newLink data)
          link-new-id (:id link-new)
          links-prev (:links db)
          link-prev (filter (fn [link] (= link-new-id (:id link))) links-prev)
          size (count link-prev)
          already-there (> size 0)]
      (if-let [not-there-yet (not already-there)]
        (let [links (conj links-prev link-new)]
          (-> db
              (assoc :loading? false)
              (assoc :links links)))
        db))))

(re-frame/reg-event-db
  ::on-new-vote
  (fn [db [_ {:keys [data errors] :as payload}]]
    (let [vote-new (:newVote data)
          vote-new-id (:id vote-new)
          link-id (get-in vote-new [:link :id])
          usr (:user vote-new)
          usr-id (:id usr)
          links (:links db)
          link (first (filter (fn [link] (= link-id (:id link))) links))
          votes (:votes link)
          votes-by-usr (filter (fn [vote] (= usr-id (get-in vote [:user :id]))) votes)
          size (count votes-by-usr)
          already-there (> size 0)]
      (if-let [not-there-yet (not already-there)]
        (let [vote-new {:id vote-new-id :user usr}
              links-updated (map
                             (fn [link]
                               (if (= link-id (:id link))
                                 (update link :votes conj vote-new)
                                 link
                                 ))
                             links)]
            (-> db
              (assoc :loading? false)
              (assoc :links links-updated)))
        db))))

(re-frame/reg-event-fx
 :fetch-new-links
 (fn [{:keys [db]} [_ page]]
   (let [first new-links-per-page
         pg (if (nil? page) 1 page)
         skip (* new-links-per-page (- pg 1))]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/query
                 queries/feed
                  {:first first :skip skip}
                 [::on-feed-new]]})))

(re-frame/reg-event-fx
 ::on-feed-new
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [links (get-in data [:feed :links])
         count (get-in data [:feed :count])]
     {:db (-> db
              (assoc :loading? false)
              (assoc :new-links links)
              (assoc :count count))
      :dispatch [::set-active-panel :new-panel]})))

(re-frame/reg-event-fx
 :fetch-top-links
 (fn [{:keys [db]} [_ _]]
   (let [first top-links-per-page
         skip 1]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/query
                 queries/feed
                  {:first first :skip skip}
                 [::on-feed-top]]})))

(re-frame/reg-event-fx
 ::on-feed-top
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
      :dispatch [::set-active-panel :top-panel]})))

(re-frame/reg-event-db
  :create-link
  (fn-traced [db  [_ description url]]
    (re-frame/dispatch
     [::re-graph/mutate
       mutations/post
       {:url url
        :description description}
       [::on-create-link]])
     (-> db
         (assoc :loading? true)
         (assoc :error false))))

(re-frame/reg-event-db
  ::on-create-link
  (fn [db [_ {:keys [data errors] :as payload}]]
    (-> db
        (assoc :loading? false)
        (assoc :link (:post data)))))

(re-frame/reg-event-db
  :signup
  (fn-traced [db  [_ name email password]]
    (re-frame/dispatch
     [::re-graph/mutate
       mutations/signup
       {:email email
        :password password
        :name name}
       [::on-signup]])
     (-> db
         (assoc :loading? true)
         (assoc :error false))))

(re-frame/reg-event-db
  ::on-signup
  (fn [db [_ {:keys [data errors] :as payload}]]
    (re-frame/dispatch [::set-active-panel :new-panel])
    (let [token (get-in data [:signup :token])]
      (set-item local-storage "token" token)
      (-> db
          (assoc :loading? false)
          (assoc :token token)))))

(re-frame/reg-event-db
  :login
  (fn-traced [db  [_ email password]]
    (re-frame/dispatch
      [::re-graph/mutate
       mutations/login
       {:email email
        :password password}
       [::on-login]])
     (-> db
         (assoc :loading? true)
         (assoc :error false))))

(re-frame/reg-event-db
  ::on-login
  (fn [db [_ {:keys [data errors] :as payload}]]
    (re-frame/dispatch [::set-active-panel :new-panel])
    (let [token (get-in data [:login :token])]
      (set-item local-storage "token" token)
      (-> db
          (assoc :loading? false)
          (assoc :token token)))))

(re-frame/reg-event-db
 :search-links
  (fn-traced [db  [_ filter]]
    (re-frame/dispatch
     [::re-graph/query
      queries/search
       {:filter filter}
       [::on-feed]])
     (-> db
         (assoc :loading? true)
         (assoc :error false))))

(re-frame/reg-event-db
  ::on-search-links
  (fn [db [_ {:keys [data errors] :as payload}]]
    (let [links (get-in data [:feed :links])]
      (-> db
          (assoc :loading? false)
          (assoc :search-links links)))))
