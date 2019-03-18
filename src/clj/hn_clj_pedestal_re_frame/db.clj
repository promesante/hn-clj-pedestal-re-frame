(ns hn-clj-pedestal-re-frame.db
  (:require
   [com.stuartsierra.component :as component]
   [yesql.core :as yesql])
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
           :connection (pooled-data-source "localhost" "hndb" "hn_role" "lacinia" 5432)))

  (stop [this]
    (-> ds :datasource .close)
    (assoc this :connection nil)))

(defn new-db
  []
  {:db (map->HackerNewsDb {})})
