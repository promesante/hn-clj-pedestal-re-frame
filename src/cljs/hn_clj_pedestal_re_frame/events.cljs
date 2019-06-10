(ns hn-clj-pedestal-re-frame.events
  (:require
   [re-frame.core :as re-frame]
   [re-graph.core :as re-graph]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [hodgepodge.core :refer [local-storage set-item remove-item]]
   [cljs-time.format :as format]
   [hn-clj-pedestal-re-frame.db :as db]))

(def links-per-page 5)

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-fx
 ::init-alt
 (fn [{:keys [db]} [_ _]]
   {:db (-> db
            (assoc :loading? true)
            (assoc :error false))
    :dispatch [::re-graph/query
               "{
                  feed {
                    count
                    links {
                      id
                      created_at
                      url
                      description
                      posted_by {
                        id
                        name
                      }
                      votes {
                        id
                        user {
                          id
                        }
                      }
                    }
                  }
                }"
                {}
                [::on-feed]]}))

(re-frame/reg-event-fx
 ::init
; ::on-feed
 (fn [{:keys [db]} [_ _]]
; (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
;   (let [links (get-in data [:feed :links])]
     {:db (-> db
              (assoc :loading? true)
;              (assoc :loading? false)
              (assoc :error false))
;              (assoc :links links))
      :dispatch-n (list
                   [::re-graph/subscribe
                    :subscribe-to-new-links
                    "{
                      newLink {
                        id
                        url
                        description
                        created_at
                        posted_by {
                          id
                          name
                        }
                      }
                    }"
                    {}
                    [::on-new-link]]
                   [::re-graph/subscribe
                    :subscribe-to-new-votes
                    "{
                      newVote {
                        id
                        link {
                          id
                          url
                          description
                          created_at
                          posted_by {
                            id
                            name
                          }
                          votes {
                            id
                            user {
                              id
                            }
                          }
                        }
                        user {
                          id
                        }
                      }
                    }"
                    {}
                    [::on-new-vote]]
                   )}))
       
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
   (let [first links-per-page
         skip (* links-per-page (- page 1))]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/query
                 "FeedQuery($first: Int, $skip: Int) {
                    feed(
                      first: $first,
                      skip: $skip
                    ) {
                      count
                      links {
                        id
                        created_at
                        url
                        description
                        posted_by {
                          id
                          name
                        }
                        votes {
                          id
                          user {
                            id
                          }
                        }
                      }
                    }
                  }"
                  {:first first :skip skip}
                 [::on-feed]
                 ]})))

(re-frame/reg-event-fx
 ::on-feed
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [links (get-in data [:feed :links])]
     {:db (-> db
              (assoc :loading? false)
              (assoc :links links))
      :dispatch [::set-active-panel :link-list-panel]})))

(re-frame/reg-event-db
  :create-link
  (fn-traced [db  [_ description url]]
    (re-frame/dispatch
      [::re-graph/mutate
       "post($url:String!, $description:String!) {
          post(
            url: $url,
            description: $description
          ) {
            id
          }
        }"
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
       "signup($email:String!, $password:String!, $name:String!) {
          signup(
            email: $email,
            password: $password,
            name: $name
          ) {
            token
          }
        }"
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
    (re-frame/dispatch [::set-active-panel :link-list-panel])
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
       "login($email:String!, $password:String!) {
          login(
            email: $email,
            password: $password
          ) {
            token
          }
        }"
       {:email email
        :password password
        }
       [::on-login]])
     (-> db
         (assoc :loading? true)
         (assoc :error false))))

(re-frame/reg-event-db
  ::on-login
  (fn [db [_ {:keys [data errors] :as payload}]]
    (re-frame/dispatch [::set-active-panel :link-list-panel])
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
       "FeedSearchQuery($filter: String!) {
          feed(filter: $filter) {
            links {
              id
              url
              description
              created_at
              posted_by {
                id
                name
              }
              votes {
                id
                user {
                  id
                }
              }
            }
          }
       }"
       {:filter filter}
       [::on-feed]
;       [::on-search-links]
       ])
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
