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
  (let [[sql & params] statement]
    (log/debug :sql (str/replace sql #"\s+" " ")
               :params params))
  (jdbc/query (:ds component) statement))

(defn list-links
  [component]
  (query component
;    (jdbc/query (:ds component)
       ["select id, description, url, created_at, updated_at from link"]))

(defn insert-link
  [component url description]
  (jdbc/insert! (:ds component) :link
                {:description description :url url}))

(defn insert-user
  [component email password name]
  (jdbc/insert! (:ds component) :usr
                {:email email :password password :name name}))

(defn ^:private apply-link
  [links link]
  (cons link links))
