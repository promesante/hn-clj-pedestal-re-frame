(ns hn-clj-pedestal-re-frame.events.graph-ql.mutations
  (:require
   [re-frame.core :as re-frame]
   [re-graph.core :as re-graph]
   [hn-clj-pedestal-re-frame.events.utils :as utils]
   [hn-clj-pedestal-re-frame.db :as db]
   [hn-clj-pedestal-re-frame.config :as config]
   [hn-clj-pedestal-re-frame.graph-ql.mutations :as mutations]))


;-------------------------------------------------------------------------
; Link Creation
;-------------------------------------------------------------------------

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
                 [:on-create-link]]}))

(re-frame/reg-event-fx
 :on-create-link
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
     {:db (-> db
              (assoc :loading? false)
              (assoc :link (:post data)))
      :dispatch [:navigate :home]}))


;-------------------------------------------------------------------------
; Link Votation
;-------------------------------------------------------------------------

(re-frame/reg-event-fx
 :vote-link
 (fn [{:keys [db]} [_ link-id]]
     {:db (-> db
             (assoc :loading? true)
             (assoc :error false))
      :dispatch [::re-graph/mutate
                 mutations/vote
                 {:link_id link-id}
                 [:on-vote-link]]}))

(re-frame/reg-event-fx
 :on-vote-link
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [vote-new (:vote data)]
     {:dispatch [:on-new-vote-db vote-new]})))

(re-frame/reg-event-db
  :on-new-vote-db
  (fn [db [_ vote-new]]
    (let [links (:new-links db)
          voted? (utils/voted? vote-new links)]
      (if voted?
        db
        (let [links-updated (utils/add-vote vote-new links)]
            (-> db
              (assoc :loading? false)
              (assoc :new-links links-updated)))))))


;-------------------------------------------------------------------------
; Signup
;-------------------------------------------------------------------------

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
                 [:on-signup]]}))

(re-frame/reg-event-fx
 :on-signup
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [token (get-in data [:signup :token])
         authorization (str "Bearer " token)
         headers {"Authorization" authorization}
         http-parameters {:headers headers}]
     {:db (-> db
              (assoc :loading? false)
              (assoc-in
                config/token-header-path
                headers))
      :dispatch [:navigate :home]
      :set-local-store ["token" token]})))


;-------------------------------------------------------------------------
; Login
;-------------------------------------------------------------------------

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
                 [:on-login]]}))

(re-frame/reg-event-fx
 :on-login
 (fn [{:keys [db]} [_ {:keys [data errors] :as payload}]]
   (let [token (get-in data [:login :token])
         authorization (str "Bearer " token)
         headers {"Authorization" authorization}
         http-parameters {:headers headers}]
     {:db (-> db
              (assoc :loading? false)
              (assoc-in
                config/token-header-path
                headers))
      :set-local-store ["token" token]
      :dispatch [:navigate :home]})))
