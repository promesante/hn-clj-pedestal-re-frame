(ns hn-clj-pedestal-re-frame.events
  (:require
   [re-frame.core :as re-frame]
   [re-graph.core :as re-graph]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [hodgepodge.core :refer [local-storage get-item set-item remove-item]]
   [cljs-time.format :as format]
   [hn-clj-pedestal-re-frame.routes :as routes]
   [hn-clj-pedestal-re-frame.db :as db]
   [hn-clj-pedestal-re-frame.config :as config]
   [hn-clj-pedestal-re-frame.graph-ql.queries :as queries]
   [hn-clj-pedestal-re-frame.graph-ql.mutations :as mutations]
   [hn-clj-pedestal-re-frame.graph-ql.subscriptions :as subscriptions]))


;-----------------------------------------------------------------------
; Core
;-----------------------------------------------------------------------

(re-frame/reg-event-db
 :set-active-panel
 (fn-traced [db [_ active-panel]]
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


;-----------------------------------------------------------------------
; Initialization
;-----------------------------------------------------------------------

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
                   [::re-graph/init
                    {:http-parameters
                     {:with-credentials? false
                      :headers {"Authorization" "Bearer <token>"}}}]
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

       
;-----------------------------------------------------------------------
; Utility Functions
;-----------------------------------------------------------------------

(defn created? [link links]
  (let [link-id (:id link)
        created? (some #(= link-id (:id %)) links)]
    created?))

(defn handle-drop [links]
  (if (> (count links) config/new-links-per-page)
    (drop-last links)
    links))

(defn add-link [link links]
  (let [link-vector [link]
        links-before-drop (concat link-vector links)
        links-after-drop (handle-drop links-before-drop)]
    links-after-drop))

(defn voted? [vote links]
  (let [link-id (get-in vote [:link :id])
        usr (:user vote)
        usr-id (:id usr)
        link (first (filter (fn [link] (= link-id (:id link))) links))
        votes (:votes link)
        voted? (some #(= usr-id (get-in % [:user :id])) votes)]
    voted?))

(defn add-vote [vote links]
  (let [link-id (get-in vote [:link :id])
        usr (:user vote)
        vote-id (:id vote)
        new-vote {:id vote-id :user usr}
        links-updated (map
                       (fn [link]
                         (if (= link-id (:id link))
                           (update link :votes conj new-vote)
                           link))
                       links)]
    links-updated))


;-----------------------------------------------------------------------
; GraphQL Subscriptions
;-----------------------------------------------------------------------

(re-frame/reg-event-db
  ::on-new-link
  (fn [db [_ {:keys [data errors] :as payload}]]
    (let [link-new (:newLink data)
          links-prev (:new-links db)
          created? (created? link-new links-prev)]
      (if created?
        db
        (let [links (add-link link-new links-prev)]
          (-> db
              (assoc :loading? false)
              (assoc :new-links links)))))))

(re-frame/reg-event-fx
 ::on-new-vote
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [vote-new (:newVote data)]
     {:dispatch [::on-new-vote-db vote-new]})))


;-----------------------------------------------------------------------
; GraphQL Queries
;-----------------------------------------------------------------------

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
      :dispatch [:set-active-panel :new-panel]})))

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
      :dispatch [:set-active-panel :top-panel]})))

(re-frame/reg-event-fx
 :search-links
 (fn [{:keys [db]} [_ filter]]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/query
                 queries/search
                 {:filter filter}
                 [::on-search-links]]}))

(re-frame/reg-event-db
  ::on-search-links
  (fn [db [_ {:keys [data errors] :as payload}]]
    (let [links (get-in data [:feed :links])]
      (-> db
          (assoc :loading? false)
          (assoc :search-links links)))))


;-----------------------------------------------------------------------
; GraphQL Mutations
;-----------------------------------------------------------------------

(re-frame/reg-event-fx
 :create-link
 (fn [{:keys [db]} [_ description url]]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/mutate
                 mutations/post
                 {:url url
                  :description description}
                 [::on-create-link]]}))

(re-frame/reg-event-fx
 ::on-create-link
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
     {:db (-> db
              (assoc :loading? false)
              (assoc :link (:post data)))
      :dispatch [:navigate :home]}))

(re-frame/reg-event-fx
 :vote-link
 (fn [{:keys [db]} [_ link-id]]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/mutate
                 mutations/vote
                 {:link_id link-id}
                 [::on-vote-link]]}))

(re-frame/reg-event-fx
 ::on-vote-link
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [vote-new (:vote data)]
     {:dispatch [::on-new-vote-db vote-new]})))

(re-frame/reg-event-db
  ::on-new-vote-db
  (fn [db [_ vote-new]]
    (let [links (:new-links db)
          voted? (voted? vote-new links)]
      (if voted?
        db
        (let [links-updated (add-vote vote-new links)]
            (-> db
              (assoc :loading? false)
              (assoc :new-links links-updated)))))))

(re-frame/reg-event-fx
 :signup
 (fn [{:keys [db]} [_ name email password]]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/mutate
                 mutations/signup
                 {:email email
                  :password password
                  :name name}
                 [::on-signup]]}))

(re-frame/reg-event-fx
 ::on-signup
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [token (get-in data [:signup :token])
         authorization (str "Bearer " token)
         headers {"Authorization" authorization}
         http-parameters {:headers headers}]
     {:db (-> db
              (assoc :loading? false)
              (assoc-in
                [:re-graph :re-graph.internals/default :http-parameters :headers]
                headers)
              (assoc :auth? true))
      :dispatch [:navigate :home]
      :set-local-store ["token" token]})))

(re-frame/reg-event-fx
 :login
 (fn [{:keys [db]} [_ email password]]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/mutate
                 mutations/login
                 {:email email
                  :password password}
                 [::on-login]]}))

(re-frame/reg-event-fx
 ::on-login
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [token (get-in data [:login :token])
         authorization (str "Bearer " token)
         headers {"Authorization" authorization}
         http-parameters {:headers headers}]
     {:db (-> db
              (assoc :loading? false)
              (assoc-in
                [:re-graph :re-graph.internals/default :http-parameters :headers]
                headers)
              (assoc :auth? true))
      :dispatch [:navigate :home]
      :set-local-store ["token" token]})))

(re-frame/reg-event-fx
 :logout
 (fn [{:keys [db]} _]
     {:remove-local-store ["token"]
      :db (-> db
              (assoc :loading? false)
              (assoc :auth? false))}))
