(ns hn-clj-pedestal-re-frame.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [re-graph.core :as re-graph]
   [hn-clj-pedestal-re-frame.events :as events]
   [hn-clj-pedestal-re-frame.routes :as routes]
   [hn-clj-pedestal-re-frame.views :as views]
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
  (routes/app-routes)
  ;; initialise re-graph, including configuration options
  (re-frame/dispatch [::re-graph/init {:resume-subscriptions?   false
                                       :connection-init-payload {}}])
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::events/subscribe-2-new-links])
  (dev-setup)
  (mount-root))
