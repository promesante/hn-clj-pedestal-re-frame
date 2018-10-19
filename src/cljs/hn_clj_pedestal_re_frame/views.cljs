(ns hn-clj-pedestal-re-frame.views
  (:require
   [re-frame.core :as re-frame]
   [hn-clj-pedestal-re-frame.subs :as subs]))

(defn links-panel []
  (let [links (re-frame/subscribe [::subs/links])]
    [:div.flex.mt2.items-start
      [:div.ml1
        [:ul
         (map (fn [link]
                [:li {:key (:id link)}
                 [:a {:href (:url link)} (:description link)]])
              @links)]]]))

;; home
(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 (str "Hello from " @name ". This is the Home Page.")]
     [:div
      [:a {:href "#/about"}
       "go to About Page"]]
     ]))

;; about
(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:href "#/"}
     "go to Home Page"]]])

;; main
(defn- panels [panel-name]
  (case panel-name
    :links-panel [links-panel]
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:div.ph3.pv1.background-gray
     [show-panel @active-panel]]))
