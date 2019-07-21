(ns hn-clj-pedestal-re-frame.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]
            [hn-clj-pedestal-re-frame.events :as events]))

(def routes ["/" 
             {"new/" {[:page] :new}
              "" :new
              "search" :search
              "create" :create
              "login" :login
              "top" :top
              true :not-found}])

(def panel-sufix "-panel")
(def events {:new #(re-frame/dispatch [:fetch-new-links (:page %)])
             :top #(re-frame/dispatch [:fetch-top-links])})

(defn switch-panel [panel-id]
  (let [panel-name (keyword (str (name panel-id) panel-sufix))]
    (re-frame/dispatch [::events/set-active-panel panel-name])))

(defn handle-match [match]
  (let [{:keys [handler route-params]} match
        event (get events handler)]
    (if (nil? event)
      (switch-panel handler)
      (event route-params))))

(defn bidi-matcher
  "Will match a URL to a route"
  [s]
  (let [match (bidi/match-route routes s)]
    match))

(def history
  (pushy/pushy handle-match bidi-matcher))

(defn start-history! []
  (pushy/start! history))

(def url-for (partial bidi/path-for routes))
