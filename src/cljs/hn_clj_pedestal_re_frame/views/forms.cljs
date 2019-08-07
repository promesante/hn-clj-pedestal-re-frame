(ns hn-clj-pedestal-re-frame.views.forms
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]))


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
        [:input.mb2 {:type "password"
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
