(ns hn-clj-pedestal-re-frame.views.lists
  (:require
   [clojure.string :as str]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [hn-clj-pedestal-re-frame.routes :as routes]
   [hn-clj-pedestal-re-frame.subs :as subs]
   [hn-clj-pedestal-re-frame.config :as config]
   [hn-clj-pedestal-re-frame.views.utils :as utils]))


;-----------------------------------------------------------------------
; Utils
;-----------------------------------------------------------------------

(defn parse-page []
  (let [path-name js/window.location.pathname
        path-elems (str/split path-name #"/")
        path-length (count path-elems)]
    (if (and (= path-length 3) (= "new" (get path-elems 1)))
      (js/parseInt (get path-elems 2))
      1)))

(defn parse-path []
  (let [path-name js/window.location.pathname
        path-elems (str/split path-name #"/")
        path (get path-elems 1)]
    path))


;-----------------------------------------------------------------------
; Record
;-----------------------------------------------------------------------

(defn link-record []
  (fn [idx link]
    (let [{:keys [id created_at description url posted_by votes]} link
          link-id (js/parseInt id)
          auth? (re-frame/subscribe [:auth?])
          time-diff (utils/time-diff-for-date created_at)
          path (parse-path)
          new? (or (nil? path) (= "new" path))]
      [:div.flex.mt2.items-start
       [:div.flex.items-center
        [:span.gray (str (inc idx) ".")]
        (when (and @auth? new?)
          [:div.f6.lh-copy.gray
            {:on-click #(re-frame/dispatch [:vote-link link-id])} "▲"])
        [:div.ml1
         [:div (str description " (" url ")")]
         [:div.f6.lh-copy.gray
          (str (count votes) " votes | by " (:name posted_by) " " time-diff)]]]])))


;-----------------------------------------------------------------------
; Lists
;-----------------------------------------------------------------------

(defn new-panel []
  (let [links (re-frame/subscribe [:new-links])
        count (re-frame/subscribe [:count])
        auth? (re-frame/subscribe [:auth?])
        page (parse-page)
        last (quot @count config/new-links-per-page)]
    [:div
     (when @auth? [:div ""])
     (map-indexed (link-record) @links)
     [:div.flex.ml4.mv3.gray
      (when (> page 1)
        [:a.ml1.no-underline.gray
         {:href (routes/url-for :new :page (- page 1))} "Previous"])
      (when (< page last)
        [:a.ml1.no-underline.gray
         {:href (routes/url-for :new :page (+ page 1))} "Next"])]]))

(defn top-panel []
  (let [links (re-frame/subscribe [:top-links])]
    [:div
     (map-indexed (link-record) @links)]))

(defn search-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        error? (re-frame/subscribe [:error?])
        links (re-frame/subscribe [:search-links])
        filter (reagent/atom "")
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
       (map-indexed (link-record) @links)
       (when @error?
         [:p.error-text.text-danger "Error in search"])])))
