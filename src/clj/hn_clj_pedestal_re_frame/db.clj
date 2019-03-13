(ns hn-clj-pedestal-re-frame.db
  (:require
   [com.stuartsierra.component :as component]
   [io.pedestal.log :as log]
   [clojure.java.jdbc :as jdbc]
   [clojure.string :as str])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(defn ^:private pooled-data-source
  [host dbname user password port]
  {:datasource
   (doto (ComboPooledDataSource.)
     (.setDriverClass "org.postgresql.Driver" )
     (.setJdbcUrl (str "jdbc:postgresql://" host ":" port "/" dbname))
     (.setUser user)
     (.setPassword password))})

(defrecord HackerNewsDb [ds]

  component/Lifecycle

  (start [this]
    (assoc this
           :ds (pooled-data-source "localhost" "hndb" "hn_role" "lacinia" 5432)))

  (stop [this]
    (-> ds :datasource .close)
    (assoc this :ds nil)))

(defn new-db
  []
  {:db (map->HackerNewsDb {})})

(defn ^:private query
  [component statement]
  (let [[sql & params] statement
        result (jdbc/query (:ds component) statement)]
    (log/debug :sql (str/replace sql #"\s+" " ")
               :params params)
    (println (str "query - result: " result))
    result))
;  (jdbc/query (:ds component) statement))

(defn list-links
  [component]
  (query component
;    (jdbc/query (:ds component)
       ["select id, description, url, usr_id, created_at, updated_at from link"]))

(defn insert-link
  [component url description usr-id]
  (jdbc/insert! (:ds component) :link
                {:description description :url url :usr_id usr-id}))

(defn find-user-by-email
  [component email]
  (first
    (jdbc/query (:ds component)
                ["select id, name, email, password, created_at, updated_at
                  from usr where email = ?" email])))

(defn find-votes-by-link-usr
  [component link-id usr-id]
  (println (str "find-vote-by-link-usr - link-id: " link-id ", usr-id: " usr-id))
;  (first
    (jdbc/query (:ds component)
                ["select id from vote
                  where link_id = ?::integer and usr_id = ?::integer"
                 link-id usr-id]))
;                ["select
;                  l.id as link_id, l.description, l.url, l.usr_id,
;                  l.created_at as link_created_at, l.updated_at as link_updated_at,
;                  u.id as usr_id, u.name, u.email, u.password,
;                  u.created_at as usr_created_at, u.updated_at as usr_updated_at
;                  from vote v
;                  inner join link l on (v.link_id = l.id)
;                  inner join usr u on (v.usr_id = u.id)
;                  where l.id::integer = ? and u.id::integer = ?" link-id usr-id])))


(defn find-user-by-link
  [component link-id]
  (println (str "find-user-by-link - link-id: " link-id))
  (first
   (jdbc/query (:ds component)
                ["select
                  u.id, u.name, u.email, u.password, u.created_at, u.updated_at
                  from link l
                  inner join usr u
                  on (l.usr_id = u.id)
                  where l.id = ?" link-id])))

(defn find-links-by-user
  [component user-id]
;  (println (str "find-links-by-user - user-id: " user-id))
;  (first
   (jdbc/query (:ds component)
                ["select l.id, l.description, l.url, l.usr_id, l.created_at, l.updated_at
                  from link l
                  inner join usr u
                  on (l.usr_id = u.id)
                  where u.id = ?" user-id]))

(defn find-votes-by-link
  [component link-id]
  (println (str "find-votes-by-link - link-id: " link-id))
;  (first
   (jdbc/query (:ds component)
                ["select
                  l.id as link_id, l.description, l.url, l.usr_id,
                  l.created_at as link_created_at, l.updated_at as link_updated_at,
                  u.id as usr_id, u.name, u.email, u.password,
                  u.created_at as usr_created_at, u.updated_at as usr_updated_at
                  from vote v
                  inner join link l on (v.link_id = l.id)
                  inner join usr u on (v.usr_id = u.id)
                  where l.id = ?" link-id]))

(defn insert-user
  [component email password name]
  (jdbc/insert! (:ds component) :usr
                {:email email :password password :name name}))

; (defn insert-vote
;   [component link-id usr-id]
;   (jdbc/insert! (:ds component) :vote
;                {:link_id link-id :usr_id usr-id}))

(defn insert-vote
  [component link-id usr-id]
  (let [result (query component
                      ["insert into vote (link_id, usr_id)
                        values (?::integer, ?::integer)"
                       link-id usr-id])]
    (println (str "insert-vote - result: " result))
    result))

(defn ^:private apply-link
  [links link]
  (cons link links))
