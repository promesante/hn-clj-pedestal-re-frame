(ns hn-clj-pedestal-re-frame.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [hn-clj-pedestal-re-frame.events.init :as events]
   [hn-clj-pedestal-re-frame.events.authentication]
   [hn-clj-pedestal-re-frame.events.core]
   [hn-clj-pedestal-re-frame.events.init]
   [hn-clj-pedestal-re-frame.events.utils]
   [hn-clj-pedestal-re-frame.events.graph-ql.queries]
   [hn-clj-pedestal-re-frame.events.graph-ql.mutations]
   [hn-clj-pedestal-re-frame.events.graph-ql.subscriptions]
   [hn-clj-pedestal-re-frame.routes :as routes]
   [hn-clj-pedestal-re-frame.views.core :as views]
   [hn-clj-pedestal-re-frame.config :as config]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/init])
  (routes/start-history!)
  (dev-setup)
  (mount-root))


