(ns user
  (:require
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.java.browse :refer [browse-url]]
   [hn-clj-pedestal-re-frame.system :as system]
   [hn-clj-pedestal-re-frame.db :as db]
   [com.stuartsierra.component :as component]))

(defonce system (system/new-system))

; (defonce db (db/new-db))

(defn q
  [query-string]
  (-> system
      :schema-provider
      :schema
      (lacinia/execute query-string nil nil)))

(defonce db
;  []
  (-> system
;      :schema-provider
      :db))
;      (lacinia/execute query-string nil nil)))

(defn start
  []
  (alter-var-root #'system component/start-system)
  (browse-url "http://localhost:8888/")
  :started)

(defn stop
  []
  (alter-var-root #'system component/stop-system)
  :stopped)

