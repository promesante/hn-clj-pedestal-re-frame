(ns hn-clj-pedestal-re-frame.system-tests
  (:require
    [clojure.test :refer [deftest is]]
    [hn-clj-pedestal-re-frame.system :as system]
    [hn-clj-pedestal-re-frame.test-utils :refer [simplify]]
    [com.stuartsierra.component :as component]
    [com.walmartlabs.lacinia :as lacinia]))

(defn ^:private test-system
  "Creates a new system suitable for testing, and ensures that
  the HTTP port won't conflict with a default running system."
  []
  (-> (system/new-system)
      (assoc-in [:server :port] 8989)))

(defn ^:private q
  "Extracts the compiled schema and executes a query."
  [system query variables]
  (-> system
      (get-in [:schema-provider :schema])
      (lacinia/execute query variables nil)
      simplify))

(deftest can-write-link
  (let [system (component/start-system (test-system))
        results (q system
                   "mutation { post(url: \"https://macwright.org/2017/08/09/decentralize-ipfs.html\", description: \"So you want to decentralize your website with IPFS\") { id }}"
                   nil)]
    (is (= {:data
            {:feed [{:id "3",
                     :url "https://macwright.org/2017/08/09/decentralize-ipfs.html",
                     :description "So you want to decentralize your website with IPFS"}]}}
           results))
    (component/stop-system system)))

(deftest can-read-links-list
  (let [system (component/start-system (test-system))
        results (q system
                   "{ feed { id url description }}"
                   nil)]
    (is (= {:data
            {:feed [{:id "1",
                     :url "https://www.prismagraphql.com",
                     :description "INIT - Prisma turns your database into a GraphQL API"}
                    {:id "2",
                     :url "https://www.apollographql.com/docs/react/",
                     :description "INIT - The best GraphQL client"}]}}
           results))
    (component/stop-system system)))
