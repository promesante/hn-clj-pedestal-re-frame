(ns hn-clj-pedestal-re-frame.sql
  (:require [yesql.core :as yesql]))

(yesql/defqueries "sql/queries.sql")
