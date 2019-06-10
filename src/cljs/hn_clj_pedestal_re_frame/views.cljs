(ns hn-clj-pedestal-re-frame.views
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [hodgepodge.core :refer [local-storage get-item remove-item]]
   [hn-clj-pedestal-re-frame.subs :as subs]
   [hn-clj-pedestal-re-frame.utils :as utils]))

(defn header-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        token (reagent/atom (get-item local-storage "token" "empty"))
        route (if (= @token "empty") "login" "")
        label (if (= @token "empty") "login" "logout")
        on-click (fn [_]
                   (remove-item local-storage "token")
                   (reset! token "empty"))]
    [:div.flex.pa1.justify-between.nowrap.orange
     [:div.flex.flex-fixed.black
      [:div.fw7.mr1 "Hacker News"]
      [:a.ml1.no-underline.black {:href "#/"} "new"]
      [:div.ml1 "|"]
;      [:a.ml1.no-underline.black {:href "#/top"} "top"]
;      [:div.ml1 "|"]
      [:a.ml1.no-underline.black {:href "#/search"} "search"]
      [:div.ml1 "|"]
      [:a.ml1.no-underline.black {:href "#/create"} "submit"]]
     [:div.flex.flex-fixed
      [:a.ml1.no-underline.black
       {:href (str "#/" route)
        :on-click #(when-not @loading? (on-click %))}
       label]]]))

(defn link-record
  [token]
  (fn [idx link]
    (let [{:keys [id created_at description url posted_by votes]} link
          time-diff (utils/time-diff-for-date created_at)]
      [:div.flex.mt2.items-start
       [:div.flex.items-center
        [:span.gray (str (inc idx) ".")]
        (when (not (= token "empty"))
          [:div.f6.lh-copy.gray "▲"])
        [:div.ml1
         [:div (str description " (" url ")")]
         [:div.f6.lh-copy.gray
          (str (count votes) " votes | by " (:name posted_by) " " time-diff)]]]])))

(defn link-list-panel []
  (let [links (re-frame/subscribe [::subs/links])
        token (reagent/atom (get-item local-storage "token" "empty"))
        pathname js/window.location.pathname]
    (println (str "pathname: " pathname))
    [:div
     (map-indexed (link-record @token)
      @links)]))

(defn search-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        error? (re-frame/subscribe [:error?])
        links (re-frame/subscribe [::subs/search-links])
        filter (reagent/atom "")
        token (reagent/atom (get-item local-storage "token" "empty"))
        on-click (fn [_]
                   (when-not (or (empty? @filter))
                     (re-frame/dispatch [:search-links @filter])
                     (reset! filter "")))]
    (fn []
      [:div
       [:div "Search"
        [:input {:type "text"
                 :on-change #(reset! filter (-> % .-target .-value))}]
        [:button {:type "button"
                  :on-click #(when-not @loading? (on-click %))}
          "OK"]]
       (map-indexed (link-record @token)
                    @links)
       (when @error?
         [:p.error-text.text-danger "Error in search"])])))

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
         [:p.error-text.text-danger "Error in link creation"])])))

(defn login-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        error? (re-frame/subscribe [:error?])
        name (reagent/atom "")
        email (reagent/atom "")
        password (reagent/atom "")
        login (reagent/atom true)
        on-click-signup (fn [_]
                   (when-not (or (empty? @name) (empty? @email) (empty? @password))
                     (re-frame/dispatch [:signup @name @email @password])
                     (reset! name "")
                     (reset! email "")
                     (reset! password "")))
        on-click-login (fn [_]
                   (when-not (or (empty? @email) (empty? @password))
                     (re-frame/dispatch [:login @email @password])
                     (reset! email "")
                     (reset! password "")))]
    (fn []
      [:div
       [:h4 {:class "mv3"} (if @login "Login" "Sign Up")]
       [:div.flex.flex-column.mt3
        (when (not @login)
          [:input.mb2 {:type "text"
                       :placeholder "Your name"
                       :on-change #(reset! name (-> % .-target .-value))}])
        [:input.mb2 {:type "text"
                     :placeholder "Your email address"
                     :on-change #(reset! email (-> % .-target .-value))}]
        [:input.mb2 {:type "text"
                     :placeholder "Choose a safe password"
                     :on-change #(reset! password (-> % .-target .-value))}]
        [:span.input-group-btn
         (if @login
           [:button.btn.btn-default {:type "button"
                                     :on-click #(when-not @loading?
                                                  (on-click-login %))}
                                     "login"]
           [:button.btn.btn-default {:type "button"
                                     :on-click #(when-not @loading?
                                                  (on-click-signup %))}
                                     "create account"])
         [:button.btn.btn-default {:type "button"
                                   :on-click #(swap! login not)}
          (if @login
            "need to create an account?"
            "already have an account?")]]]
       (when @error?
         [:p.error-text.text-danger "Error in login / signup"])])))

;; main
(defn- panels [panel-name]
  (case panel-name
    :link-list-panel [link-list-panel]
    :link-create-panel [link-create-panel]
    :login-panel [login-panel]
    :search-panel [search-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:div.ph3.pv1.background-gray
      [header-panel]
      [show-panel @active-panel]]))
