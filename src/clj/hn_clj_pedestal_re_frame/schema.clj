(ns hn-clj-pedestal-re-frame.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.util :as util]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.stuartsierra.component :as component]
    [clojure.edn :as edn]
    [hn-clj-pedestal-re-frame.db :as db]))

(defn info [context arguments value]
    "This is the API of a Hackernews Clone")

(defn feed
  [db]
  (fn [_ _ _]
    (db/list-links db)))

; (defn post!
;  [links-list context arguments value]
;    (let [{:keys [url description]} arguments
;        counter (count links-list)
;        id (str "link-" counter)
;        link {:id id
;              :url url
;              :description description}]
;      (do
;        (conj links-list link)
;        link)))

(defn resolver-map
  [component]
  (let [db (:db component)]
    {:query/info info
     :query/feed (feed db)}))
;     :mutation/post! (post! db)}))

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
