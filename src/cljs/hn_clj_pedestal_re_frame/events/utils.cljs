(ns hn-clj-pedestal-re-frame.events.utils
  (:require
   [hn-clj-pedestal-re-frame.config :as config]))


(defn created? [link links]
  (let [link-id (:id link)
        created? (some #(= link-id (:id %)) links)]
    created?))

(defn handle-drop [links]
  (if (> (count links) config/new-links-per-page)
    (drop-last links)
    links))

(defn add-link [link links]
  (let [link-vector [link]
        links-before-drop (concat link-vector links)
        links-after-drop (handle-drop links-before-drop)]
    links-after-drop))

(defn voted? [vote links]
  (let [link-id (get-in vote [:link :id])
        usr (:user vote)
        usr-id (:id usr)
        link (first (filter (fn [link] (= link-id (:id link))) links))
        votes (:votes link)
        voted? (some #(= usr-id (get-in % [:user :id])) votes)]
    voted?))

(defn add-vote [vote links]
  (let [link-id (get-in vote [:link :id])
        usr (:user vote)
        vote-id (:id vote)
        new-vote {:id vote-id :user usr}
        links-updated (map
                       (fn [link]
                         (if (= link-id (:id link))
                           (update link :votes conj new-vote)
                           link))
                       links)]
    links-updated))
