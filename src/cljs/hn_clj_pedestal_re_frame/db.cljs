(ns hn-clj-pedestal-re-frame.db)

(def default-db
  {
   :name "re-frame"
   :loading? false
   :error false
   :new-links []
   :search-links []
   :top-links []
   :link {}
   :count 0
   :new? false
   })
