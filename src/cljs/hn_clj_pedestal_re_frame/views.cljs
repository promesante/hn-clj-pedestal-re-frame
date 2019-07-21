(ns hn-clj-pedestal-re-frame.views
  (:require
   [clojure.string :as str]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [hodgepodge.core :refer [local-storage get-item remove-item]]
   [hn-clj-pedestal-re-frame.routes :as routes]
   [hn-clj-pedestal-re-frame.subs :as subs]
   [hn-clj-pedestal-re-frame.events :as events]
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
      [:a.ml1.no-underline.black {:href (routes/url-for :new :page 1)} "new"]
      [:div.ml1 "|"]
      [:a.ml1.no-underline.black {:href "/top"} "top"]
      [:div.ml1 "|"]
      [:a.ml1.no-underline.black {:href (routes/url-for :search)} "search"]
      [:div.ml1 "|"]
      [:a.ml1.no-underline.black {:href (routes/url-for :create)} "submit"]]
     [:div.flex.flex-fixed
      [:a.ml1.no-underline.black
       {:href (str "/" route)
        :on-click #(when-not @loading? (on-click %))}
       label]]
     ]))

(defn link-record [token]
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
          (str (count votes) " votes | by " (:name posted_by) " " time-diff)]]
        ]])))

(defn parse-page []
  (let [path-name js/window.location.pathname
        path-elems (str/split path-name #"/")
        path-length (count path-elems)]
    (if (and (= path-length 3) (= "new" (get path-elems 1)))
      (js/parseInt (get path-elems 2))
      1)))

(defn new-panel []
  (let [links (re-frame/subscribe [::subs/new-links])
        count (re-frame/subscribe [:count])
        token (reagent/atom (get-item local-storage "token" "empty"))
        page (parse-page)
        links-per-page events/new-links-per-page
        last (quot @count links-per-page)]
    [:div
     (map-indexed (link-record @token)
                  @links)
     [:div.flex.ml4.mv3.gray
      (when (> page 1)
        [:a.ml1.no-underline.gray
         {:href (routes/url-for :new :page (- page 1))} "Previous"])
      (when (< page last)
        [:a.ml1.no-underline.gray
         {:href (routes/url-for :new :page (+ page 1))} "Next"])]]))

(defn top-panel []
  (let [links (re-frame/subscribe [::subs/top-links])
        token (reagent/atom (get-item local-storage "token" "empty"))
        ]
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

(defn create-panel []
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
                     (reset! password "")))
        ]
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
    :new-panel [new-panel]
    :top-panel [top-panel]
    :create-panel [create-panel]
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
