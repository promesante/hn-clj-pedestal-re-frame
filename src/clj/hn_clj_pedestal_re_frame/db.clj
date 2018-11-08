(ns hn-clj-pedestal-re-frame.db
  (:require
    [com.stuartsierra.component :as component]
    [clojure.java.jdbc :as jdbc])
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

(defn list-links
  [component]
    (jdbc/query (:ds component)
       ["select id, description, url, created_at, updated_at from link"]))

(defn insert-link
  [component url description]
  (jdbc/insert! (:ds component) :link
                {:description description :url url}))

(defn ^:private apply-link
  [links link]
  (cons link links))
