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
    [hn-clj-pedestal-re-frame.db :as db]))

(def jwt-secret "GraphQL-is-aw3some")

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
      (db/list-links db)))

(defn post!
  [db]
  (fn [context arguments _]
    (let [{:keys [url description]} arguments
          usr-id (get-user-id context)
          result (db/insert-link db url description usr-id)
          [first] result]
      first)))

(defn signup!
  [db]
  (fn [_ arguments _]
    (let [{:keys [email password name]} arguments
          encrypted-password (hs/derive password)
          result (db/insert-user db email encrypted-password name)
          [user] result
          token (jwt/sign {:user-id (:id user)} jwt-secret)]
      {:token token
       :user user})))

(defn login!
  [db]
  (fn [_ arguments _]
    (if-let [user (db/find-user-by-email db (:email arguments))]
      (if-let [valid (hs/check (:password arguments) (:password user))]
        (let [token (jwt/sign {:user-id (:id user)} jwt-secret)]
          {:token token
           :user  user})
        (log/info :error "Wrong password"))
       (log/info :error "User not found"))))

(defn link-user
  [db]
  (fn [_ _ link]
    (db/find-user-by-link db (:id link))))

(defn user-links
  [db]
  (fn [_ _ user]
    (db/find-links-by-user db (:id user))))

(defn resolver-map
  [component]
  (let [db (:db component)]
    {:query/info info
     :query/feed (feed db)
     :mutation/post! (post! db)
     :mutation/signup! (signup! db)
     :mutation/login! (login! db)
     :Link/user (link-user db)
     :User/links (user-links db)
     }))

(defn load-schema
  [component]
  (-> (io/resource "hn-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
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
