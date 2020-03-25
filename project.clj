(defproject hn-clj-pedestal-re-frame "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.walmartlabs/lacinia-pedestal "0.13.0"]
                 [org.postgresql/postgresql "42.2.5.jre7"]
                 [com.mchange/c3p0 "0.9.5.2"]
                 [io.aviso/logging "0.3.1"]
                 [buddy/buddy-sign "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [kibu/pushy "0.3.8"]
                 [bidi "2.1.2"]
                 [reagi "0.10.1" :exclusions [org.clojure/clojure]]
                 [yesql "0.5.3"]
                 [hodgepodge "0.1.3"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.5"]
                 [re-graph "0.1.9"]
;                 [re-graph "0.1.9-SNAPSHOT"]
                 ]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [day8.re-frame/re-frame-10x "0.3.3"]
                   [day8.re-frame/tracing "0.5.1"]
                   [figwheel-sidecar "0.5.16"]
                   [cider/cider-nrepl "0.16.0"]
                   [cider/piggieback "0.4.0"]
                   [re-frisk "0.5.3"]]

    :plugins      [[lein-figwheel "0.5.16"]]}
   :prod { :dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}
   }

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "hn-clj-pedestal-re-frame.core/mount-root"}
     :compiler     {:main                 hn-clj-pedestal-re-frame.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           day8.re-frame-10x.preload
                                           re-frisk.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true
                                           "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            hn-clj-pedestal-re-frame.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}


    ]}
  )
