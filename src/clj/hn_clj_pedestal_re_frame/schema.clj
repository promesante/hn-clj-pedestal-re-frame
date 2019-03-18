(ns hn-clj-pedestal-re-frame.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojure.edn :as edn]
    [com.walmartlabs.lacinia.util :as util]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.stuartsierra.component :as component]
    [buddy.hashers :as hs]
    [buddy.sign.jwt :as jwt]
    [io.pedestal.log :as log]
    [reagi.core :as r]
    [hn-clj-pedestal-re-frame.db :as db]
    [hn-clj-pedestal-re-frame.sql :as sql]))

(def jwt-secret "GraphQL-is-aw3some")

(def link-events (r/events))
(def vote-events (r/events))

(defn info [context arguments value]
    "This is the API of a Hackernews Clone")

(defn to-keyword 
  [my-map]
  (into {}
        (for [[k v] my-map]
          [(keyword k) v])))

(defn get-user-id
  [context]
    (let [headers (to-keyword (get-in context [:request :headers]))
          authorization (:authorization headers)
          token (string/replace-first authorization #"Bearer " "")
          tuple (jwt/unsign token jwt-secret)
          user-id (:user-id tuple)]
      user-id))

(defn feed
  [db]
  (fn [_ _ _]
      (sql/list-links {} db)))

(defn post!
  [db]
  (fn [context arguments _]
    (let [{:keys [url description]} arguments
          usr-id (get-user-id context)
          result (sql/insert-link<! {:? [usr-id]
                                     :description description
                                     :url url}
                                    db)
          [first] result]
      (r/deliver link-events first)
      first)))

(defn signup!
  [db]
  (fn [_ arguments _]
    (let [{:keys [email password name]} arguments
          encrypted-password (hs/derive password)
          user (sql/insert-user<! {:email email
                                   :password encrypted-password
                                   :name name}
                                  db)
          token (jwt/sign {:user-id (:id user)} jwt-secret)]
      {:token token
       :user user})))

(defn login!
  [db]
  (fn [_ arguments _]
    (if-let [user (sql/find-user-by-email {:email (:email arguments)} db)]
      (if-let [valid (hs/check (:password arguments) (:password user))]
        (let [token (jwt/sign {:user-id (:id user)} jwt-secret)]
          {:token token
           :user  user})
        (log/info :error "Wrong password"))
       (log/info :error "User not found"))))

(defn vote!
  [db]
  (fn [context arguments _]
    (let [{link-id :link_id} arguments
          usr-id (get-user-id context)
          votes (sql/find-votes-by-link-usr {:? [link-id usr-id]} db)]
      (if (empty? votes)
        (let [result (sql/insert-vote<! {:? [link-id usr-id]} db)
              link (first (sql/find-link-by-id {:? [(:link_id result)]} db))
              user (first (sql/find-user-by-id {:? [(:usr_id result)]} db))
              vote {:id (:id result)
                    :link link
                    :user user}]
          (r/deliver vote-events vote)
          vote)
        (log/info :error "User's already voted for this same link")))))

(defn link-user
  [db]
  (fn [_ _ link]
    (sql/find-user-by-link {:? [(:id link)]} db)))

(defn user-links
  [db]
  (fn [_ _ user]
    (sql/find-links-by-user {:user_id (:id user)} db)))

(defn link-votes
  [db]
  (fn [_ _ link]
    (sql/find-votes-by-link {:? [(:id link)]} db)))

(defn new-link
  [db]
  (fn [context args source-stream]
    (let [new-link @link-events]
      (source-stream new-link))))

(defn new-vote
  [db]
  (fn [context args source-stream]
    (let [new-vote @vote-events]
      (source-stream new-vote))))

(defn resolver-map
  [component]
  (let [db (:db component)]
    {:query/info info
     :query/feed (feed db)
     :mutation/post! (post! db)
     :mutation/signup! (signup! db)
     :mutation/login! (login! db)
     :mutation/vote! (vote! db)
     :Link/user (link-user db)
     :Link/votes (link-votes db)
     :User/links (user-links db)}))

(defn streamer-map
 [component]
  (let [db (:db component)]
    {:subscription/new-link (new-link db)
     :subscription/new-vote (new-vote db)}))

(defn load-schema
  [component]
  (-> (io/resource "hn-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
      (util/attach-streamers (streamer-map component))
      schema/compile))

(defrecord SchemaProvider [schema]
  
  component/Lifecycle

  (start [this]
    (assoc this :schema (load-schema this)))

  (stop [this]
    (assoc this :schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (-> {}
                        map->SchemaProvider
                        (component/using [:db]))})
