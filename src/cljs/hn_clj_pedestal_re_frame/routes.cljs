(ns hn-clj-pedestal-re-frame.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]))

(def routes ["/" 
             {"" :home
              "new/" {[:page] :new}
              "top" :top
              "search" :search
              "create" :create
              "login" :login
              "logout" :logout
              true :not-found}])

(def panel-sufix "-panel")
(def events {:home #(re-frame/dispatch [:fetch-new-links 1])
             :new #(re-frame/dispatch [:fetch-new-links (:page %)])
             :top #(re-frame/dispatch [:fetch-top-links])
             :logout #(re-frame/dispatch [:logout])})

(defn switch-panel [panel-id]
  (let [panel-name (keyword (str (name panel-id) panel-sufix))]
    (re-frame/dispatch [:set-active-panel panel-name])))

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

(defn set-history! [route]
  (pushy/set-token! history (url-for route)))
