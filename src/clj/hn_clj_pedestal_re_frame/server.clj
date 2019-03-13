(ns hn-clj-pedestal-re-frame.server
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [com.walmartlabs.lacinia.pedestal :as lp]
            [io.pedestal.http :as http]))

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

(defn add-route
  [service-map]
  (let [{routes ::http/routes} service-map
        ext-routes (conj routes root-route)]
    (assoc service-map ::http/routes ext-routes)))

(defrecord Server [schema-provider server port]

  component/Lifecycle
  (start [this]
    (assoc this :server (-> schema-provider
                            :schema
                            (lp/service-map {:graphiql true
                                             :ide-path "/graphiql"
                                             :port port
                                             :subscriptions true
                                             ; :subscriptions-path "/ws"
                                             })
                            (merge {::http/resource-path "/public"})
                            (add-route)
                            http/create-server
                            http/start)))

  (stop [this]
    (http/stop server)
    (assoc this :server nil)))

(defn new-server
  []
  {:server (component/using (map->Server {:port 8888})
                            [:schema-provider])})
