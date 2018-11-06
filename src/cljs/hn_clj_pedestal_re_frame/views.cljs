(ns hn-clj-pedestal-re-frame.views
  (:require
   [re-frame.core :as re-frame]
   [hn-clj-pedestal-re-frame.subs :as subs]
   [reagent.core :as reagent]))

(defn header-panel []
  [:div.flex.pa1.justify-between.nowrap.orange
   [:div.flex.flex-fixed.black
    [:div.fw7.mr1 "Hacker News"]
    [:a.ml1.no-underline.black {:href "#/"} "new"]
    [:div.ml1 "|"]
    [:a.ml1.no-underline.black {:href "#/create"} "submit"]]])

(defn link-list-panel []
  (let [links (re-frame/subscribe [::subs/links])]
    [:div
      (map (fn [link]
        (let [{:keys [description url]} link]
;         [:div.flex.items-center
;           [:span.gray ^{:id link}]]
          [:div.flex.mt2.items-start
           [:div.ml1
             [:div (str description " (" url ")")]]]))
                @links)]))

(defn link-create-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        error? (re-frame/subscribe [:error?])
        description (reagent/atom "")
        url (reagent/atom "")
        on-click (fn [_]
                   (when-not (or (empty? @description) (empty? @url))
                     (re-frame/dispatch [:create-link @description @url])
                     (reset! description "")
                     (reset! url "")))]
    (fn []
      [:div
       [:div.flex.flex-column.mt3
        [:input.mb2 {:type "text"
                     :placeholder "A description for the link"
                     :on-change #(reset! description (-> % .-target .-value))}]
        [:input.mb2 {:type "text"
                     :placeholder "The URL for the link"
                     :on-change #(reset! url (-> % .-target .-value))}]
        [:span.input-group-btn
         [:button.btn.btn-default {:type "button"
                                   :on-click #(when-not @loading? (on-click %))}
          "Go"]
         ]]
       (when @error?
         [:p.error-text.text-danger "¯\\_(ツ)_/¯  Bad github handle or rate limited!"])])))

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
    :link-list-panel [link-list-panel]
    :link-create-panel [link-create-panel]
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:div.ph3.pv1.background-gray
      [header-panel]
      [show-panel @active-panel]]))
