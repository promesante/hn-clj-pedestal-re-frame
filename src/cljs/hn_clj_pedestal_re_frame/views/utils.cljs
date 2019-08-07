(ns hn-clj-pedestal-re-frame.views.utils
  (:require
   [cljs-time.core :as time]
   [cljs-time.format :as format]
   [cljs-time.coerce :as coerce]))

(def date-messages [
    {:conv 60 :message "less than 1 min ago"}
    {:conv 60 :message " min ago"}
    {:conv 24 :message " h ago"}
    {:conv 30 :message " days ago"}
    {:conv 365 :message " mo ago"}
    {:conv 10 :message " years ago"}])

(def mills-per-sec 1000)

(defn render-date-message
  [elapsed mills-prev messages]
  (let [current (first messages)
        pending (rest messages)
        message (:message current)
        conv (:conv current)
        mills-curr (* conv mills-prev)]
    (if (< elapsed mills-curr)
      (let [value (quot elapsed mills-prev)]
        (str value message))
      (if (nil? pending)
        (let [value (quot elapsed mills-curr)]
          (str value message))
        (render-date-message elapsed mills-curr pending)))))

(defn time-difference
  [current previous]
  (let [elapsed (- current previous)]
    (render-date-message elapsed mills-per-sec date-messages)))

(defn parse-date
  [date]
  (let [formatter (format/formatter "yyyy-MM-dd HH:mm:ss.SSS")
        sliced (subs date 0 (- (count date) 3))
        formatted (format/parse formatter sliced)]
    formatted))

(defn time-diff-for-date
  [date]
  (let [now (coerce/to-long (time/now))
        formatted (parse-date date)
        updated (coerce/to-long formatted)]
    (time-difference now updated)))
