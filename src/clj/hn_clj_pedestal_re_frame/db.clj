(ns hn-clj-pedestal-re-frame.db
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [com.stuartsierra.component :as component]))

(defrecord HackerNewsDb [data]

  component/Lifecycle

  (start [this]
    (assoc this :data (-> (io/resource "hn-data.edn")
                          slurp
                          edn/read-string
                          atom)))

  (stop [this]
    (assoc this :data nil)))

(defn new-db
  []
  {:db (map->HackerNewsDb {})})

(defn list-links
  [db]
  (->> db
       :data
       deref
       :links))

(defn ^:private apply-link
  [links link]
  (cons link links))

(defn insert-link
  "Adds a new game rating, or changes the value of an existing game rating."
  [db url description]
  (let [counter (count (list-links db))
        id (str "link-" counter)
        link {:id id
              :url url
              :description description}]
    (-> db
        :data
        (swap! update :links apply-link link))
    link))
