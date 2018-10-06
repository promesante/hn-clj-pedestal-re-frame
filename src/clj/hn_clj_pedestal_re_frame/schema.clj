(ns hn-clj-pedestal-re-frame.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.util :as util]
    [com.walmartlabs.lacinia.schema :as schema]
    [clojure.edn :as edn]))

(def links (atom
   [{:id "link-0"
     :url "www.howtographql.com"
     :description "Fullstack tutorial for GraphQL"}]))

(defn info [context arguments value]
    "This is the API of a Hackernews Clone")

(defn feed [links-list context arguments value]
    links-list)

(defn post!
  [links-list context arguments value]
    (let [{:keys [url description]} arguments
        counter (count links-list)
;        counter (count @links)
        id (str "link-" counter)
        link {:id id
              :url url
              :description description}]
      (do
        (conj links-list link)
;      (swap! links (fn [v]
;                     (conj v link)))
        link)))

(defn resolver-map []
  (let [hn-data (-> (io/resource "hn-data.edn")
                     slurp
                     edn/read-string)
        links-list (->> hn-data
                        :links)]
;                        (reduce #(assoc %1 (:id %2) %2) {}))]
    {:query/info info
     :query/feed (partial feed links-list)
     :mutation/post! (partial post! links-list)}))

(defn load-schema []
  (-> (io/resource "hn-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map))
;      (util/attach-resolvers {:query/info info
;                              :query/feed feed
;                              :mutation/post! post!})
      schema/compile))
