(ns hn-clj-pedestal-re-frame.server
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [com.walmartlabs.lacinia.pedestal :as lp]
            [io.pedestal.http :as http]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [ring.middleware.session.cookie :as cookie]))

(defn html-response
  [html]
  {:status 200 :body html :headers {"Content-Type" "text/html"}})

;; Gather some data from the user to retain in their session.
(defn index-page
  "Prompt a user for their name, then remember it."
  [req]
  (html-response
   (slurp (io/resource "public/index.html"))))


(def user-info-interceptor
  {:name ::user-info
   :enter
   (fn [{:keys [request] :as context}]
    ;; Retrieve information from for example the request
    (assoc-in context [:request :lacinia-app-context :custom-user-info-key] request))})

(defn- inject-session-interceptor [interceptors]
  (let [session-interceptor (middlewares/session {:store (cookie/cookie-store)})]
    (lp/inject interceptors user-info-interceptor :after ::lp/inject-app-context)))

(defn- inject-user-info-interceptor [interceptors]
  (lp/inject interceptors user-info-interceptor :after ::lp/inject-app-context))

(defn- interceptors [schema]
  (let [options {}
        default-interceptors (lp/default-interceptors schema options)]
    (-> default-interceptors
        (inject-session-interceptor)
        (inject-user-info-interceptor))))

(def root-route
  [["/" :get `index-page]
   ["/new/*"  :get `index-page :route-name :new]
   ["/top"    :get `index-page :route-name :top]
   ["/search" :get `index-page :route-name :search]
   ["/create" :get `index-page :route-name :create]
   ["/login"  :get `index-page :route-name :login]
   ["/logout" :get `index-page :route-name :logout]])

(defn add-route
  [service-map]
  (let [{routes ::http/routes} service-map
        ext-routes (into routes root-route)]
    (assoc service-map ::http/routes ext-routes)))

(defrecord Server [schema-provider server port]

  component/Lifecycle
  (start [this]
    (assoc this :server (-> schema-provider
                            :schema
                            (lp/service-map
                             {:graphiql true
                              :ide-path "/graphiql"
                              :port port
                              :subscriptions true
                              :interceptors (interceptors (-> schema-provider :schema))
                              :ide-headers {:authorization "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyLWlkIjozfQ.JH0Q2flkonyDPk_yiSrTK5VSKrbrsdR0FEePMgiEwDE"}
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
