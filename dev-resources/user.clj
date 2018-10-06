(ns user
  (:require
   [hn-clj-pedestal-re-frame.schema :as s]
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.pedestal :as lp]
   [io.pedestal.http :as http]
   [io.pedestal.http.ring-middlewares :as middlewares]
   [clojure.java.browse :refer [browse-url]]))

(def schema (s/load-schema))

(defn q
  [query-string]
  (lacinia/execute schema query-string nil nil))

(defonce server nil)

(defn html-response
  [html]
  {:status 200 :body html :headers {"Content-Type" "text/html"}})

;; Gather some data from the user to retain in their session.
(defn index-page
  "Prompt a user for their name, then remember it."
  [req]
  (html-response
   (slurp (io/resource "public/index.html"))))

(def root-route
  ["/" :get `index-page])
;  ["/" :get [(middlewares/resource "/index.html")]])

(defn add-route
  [service-map]
  (let [{routes ::http/routes} service-map
;        count-routes (count routes)
        ext-routes (conj routes root-route)]
;        count-ext-routes (count ext-routes)]
;    (assoc (dissoc service-map ::http/routes) ::http/routes ext-routes)))
    (assoc service-map ::http/routes ext-routes)))

(defn start-server
  [_]
  (let [server (-> schema
;                   (lp/service-map {})
                   (lp/service-map {:graphiql true
                                    :ide-path "/graphiql"})
                   (merge {::http/resource-path "/public"})
                   (add-route)
                   http/create-server
                   http/start)]
    (browse-url "http://localhost:8888/")
    server))

(defn stop-server
  [server]
  (http/stop server)
  nil)

(defn start
  []
  (alter-var-root #'server start-server)
  :started)

(defn stop
  []
  (alter-var-root #'server stop-server)
  :stopped)
