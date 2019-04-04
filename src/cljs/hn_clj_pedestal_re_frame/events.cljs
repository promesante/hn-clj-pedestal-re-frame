(ns hn-clj-pedestal-re-frame.events
  (:require
   [re-frame.core :as re-frame]
   [re-graph.core :as re-graph]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [hodgepodge.core :refer [local-storage set-item remove-item]]
   [cljs-time.format :as format]
   [hn-clj-pedestal-re-frame.db :as db]))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
  ::on-feed
  (fn [db [_ {:keys [data errors] :as payload}]]
    (let [links (get-in data [:feed :links])]
      (-> db
          (assoc :loading? false)
          (assoc :links links)))))

(re-frame/reg-event-db
 ::initialize-db
  (fn-traced [db  [_ _]]
    (re-frame/dispatch
      [::re-graph/query
       "{
          feed {
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
       [::on-feed]])
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
  ::on-signup
  (fn [db [_ {:keys [data errors] :as payload}]]
    (re-frame/dispatch [::set-active-panel :link-list-panel])
    (let [token (get-in data [:signup :token])]
      (set-item local-storage "token" token)
      (-> db
          (assoc :loading? false)
          (assoc :token token)))))

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
  ::on-login
  (fn [db [_ {:keys [data errors] :as payload}]]
    (re-frame/dispatch [::set-active-panel :link-list-panel])
    (let [token (get-in data [:login :token])]
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
  ::on-search-links
  (fn [db [_ {:keys [data errors] :as payload}]]
    (let [links (get-in data [:feed :links])]
      (-> db
          (assoc :loading? false)
          (assoc :search-links links)))))

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
       [::on-search-links]])
     (-> db
         (assoc :loading? true)
         (assoc :error false))))
