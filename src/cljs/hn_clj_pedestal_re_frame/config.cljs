(ns hn-clj-pedestal-re-frame.config)


;-----------------------------------------------------------------------
; Links per page
;-----------------------------------------------------------------------

(def new-links-per-page 5)
(def top-links-per-page 100)


;-----------------------------------------------------------------------
; Dates
;-----------------------------------------------------------------------

(def date-messages [
    {:conv 60 :message "less than 1 min ago"}
    {:conv 60 :message " min ago"}
    {:conv 24 :message " h ago"}
    {:conv 30 :message " days ago"}
    {:conv 365 :message " mo ago"}
    {:conv 10 :message " years ago"}])

(def mills-per-sec 1000)


;-----------------------------------------------------------------------
; Misc
;-----------------------------------------------------------------------

(def debug?
  ^boolean goog.DEBUG)
