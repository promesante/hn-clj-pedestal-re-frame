(ns hn-clj-pedestal-re-frame.system
  (:require
    [com.stuartsierra.component :as component]
    [hn-clj-pedestal-re-frame.schema :as schema]
    [hn-clj-pedestal-re-frame.server :as server]))

(defn new-system
  []
  (merge (component/system-map)
         (server/new-server)
         (schema/new-schema-provider)))
