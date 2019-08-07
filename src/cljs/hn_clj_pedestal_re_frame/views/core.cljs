(ns hn-clj-pedestal-re-frame.views.core
  (:require
   [re-frame.core :as re-frame]
   [hn-clj-pedestal-re-frame.views.lists :as lists]
   [hn-clj-pedestal-re-frame.views.forms :as forms]
   [hn-clj-pedestal-re-frame.routes :as routes]
   [hn-clj-pedestal-re-frame.subs :as subs]))


(defn- panels [panel-name]
  (case panel-name
    :new-panel [lists/new-panel]
    :top-panel [lists/top-panel]
    :search-panel [lists/search-panel]
    :create-panel [forms/create-panel]
    :login-panel [forms/login-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn header-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        auth? (re-frame/subscribe [:auth?])
        route (if @auth? "logout" "login")]
    [:div.flex.pa1.justify-between.nowrap.orange
     [:div.flex.flex-fixed.black
      [:div.fw7.mr1 "Hacker News"]
      [:a.ml1.no-underline.black {:href (routes/url-for :new :page 1)} "new"]
      [:div.ml1 "|"]
      [:a.ml1.no-underline.black {:href "/top"} "top"]
      [:div.ml1 "|"]
      [:a.ml1.no-underline.black {:href (routes/url-for :search)} "search"]
      [:div.ml1 "|"]
      [:a.ml1.no-underline.black {:href (routes/url-for :create)} "submit"]]
     [:div.flex.flex-fixed
      [:a.ml1.no-underline.black {:href (str "/" route)} route]]]))

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:div.ph3.pv1.background-gray
      [header-panel]
      [show-panel @active-panel]]))
