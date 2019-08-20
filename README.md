# hn-clj-pedestal-re-frame

Porting ["The Fullstack Tutorial for GraphQL"](https://www.howtographql.com), from Javascript to Clojure/script.

I have tried to match each of the articles in the series in the git commit sequence, and the description given to each of them.


## References ##

* Port source: ["The Fullstack Tutorial for GraphQL"](https://www.howtographql.com)
* Port target
  - Back-end: [Lacinia Pedestal Tutorial](https://lacinia.readthedocs.io/en/latest/tutorial/)
  - Front-end:
    * [re-frame docs](https://github.com/Day8/re-frame/blob/master/docs/README.md)
	* [re-frame tutorial](https://purelyfunctional.tv/guide/re-frame-building-blocks/)

## Setup

### Database

Database engine used is Postgresql.

Database setup is performed by setup-db.sh script in the bin directory:

```
$ ./setup-db.sh
create user hn_role password 'lacinia';
CREATE ROLE
psql:setup-db.sql:1: NOTICE:  table "link" does not exist, skipping
DROP TABLE
psql:setup-db.sql:2: NOTICE:  table "usr" does not exist, skipping
DROP TABLE
psql:setup-db.sql:3: NOTICE:  table "vote" does not exist, skipping
DROP TABLE
CREATE TABLE
INSERT 0 2
CREATE TABLE
INSERT 0 2
CREATE TABLE
INSERT 0 3
```

Checking database has been setup right:

```
$ psql -h localhost -U hn_role hndb
psql (11.4)
Type "help" for help.

hndb=> select * from usr;
 id | name |      email       | password |         created_at         |         updated_at
----+------+------------------+----------+----------------------------+----------------------------
  1 | John | john@hotmail.com | john     | 2019-08-14 16:22:03.508452 | 2019-08-14 16:22:03.508452
  2 | Paul | paul@gmail.com   | paul     | 2019-08-14 16:22:03.508452 | 2019-08-14 16:22:03.508452
(2 rows)

hndb=> select * from link;
 id |                     description                      |                    url                    | usr_id |         created_at         |         updated_at
----+------------------------------------------------------+-------------------------------------------+--------+----------------------------+----------------------------
  1 | INIT - Prisma turns your database into a GraphQL API | https://www.prismagraphql.com             |      1 | 2019-08-14 16:22:03.514718 | 2019-08-14 16:22:03.514718
  2 | INIT - The best GraphQL client                       | https://www.apollographql.com/docs/react/ |      2 | 2019-08-14 16:22:03.514718 | 2019-08-14 16:22:03.514718
(2 rows)

```


### Dependencies ###

One of the key ones has been [re-graph][re-graph], one of the front-end, clojurescript GraphQL clients. 

In order to implement GraphQL subscriptions, I've had the following to issues:

  * [Subscription to Lacinia Pedestal back end: Getting just the first event][re-graph-issue1]
  * [Queries and Mutations: websockets or HTTP?][re-graph-issue2]

To be able to go on, for each of them, I've implemented the workarounds depicted in the issues just above, and shared them in my own [fork][re-graph-fork] of re-graph. In this fork, each of those workarounds has its own commit. Hence, re-graph dependency in this project references this fork. As its JAR file is not available online, in order to have this dependency resolved, this fork should be cloned and installed locally, running `lein install` in the cloned fork's root directory.


### GraphiQL ###

On the server side, ["The Fullstack Tutorial for GraphQL"](https://www.howtographql.com) is based on [graphql-yoga](https://github.com/prisma/graphql-yoga) which, in turn, comes with [GraphQL Playground](https://github.com/prisma/graphql-playground) out of the box, as its “GraphQL IDE”.

On the other hand, [Lacinia Pedestal](https://github.com/walmartlabs/lacinia-pedestal) comes with [GraphiQL](https://github.com/graphql/graphiql).

When you need to access queries or mutations which require the user to be authenticated, whereas [GraphQL Playground](https://github.com/prisma/graphql-playground) lets you set the corresponding token in the IDE, [GraphiQL](https://github.com/graphql/graphiql) takes it as a configuration.

Furthermore, [Lacinia Pedestal](https://github.com/walmartlabs/lacinia-pedestal) lets you set it as part of its own configuration, and hands it over to [GraphiQL](https://github.com/graphql/graphiql), in [server.clj](https://github.com/promesante/hn-clj-pedestal-re-frame/blob/master/src/clj/hn_clj_pedestal_re_frame/server.clj) as `:ide-headers`, as shown below:

```clojure
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
                              :ide-headers {:authorization "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyLWlkIjozfQ.JH0Q2flkonyDPk_yiSrTK5VSKrbrsdR0FEePMgiEwDE"}
                              })
                            (merge {::http/resource-path "/public"})
                            (add-route)
                            http/create-server
                            http/start)))
```



### Starting Up ###

In two different shell sessions:

 * Front-end:

```
$ lein clean
$ lein figwheel
Figwheel: Cutting some fruit, just a sec ...
Figwheel: Validating the configuration found in project.clj
Figwheel: Configuration Valid ;)
Figwheel: Starting server at http://0.0.0.0:3449
Figwheel: Watching build - dev
Figwheel: Cleaning build - dev
Compiling build :dev to "resources/public/js/compiled/app.js" from ["src/cljs"]...
Successfully compiled build :dev to "resources/public/js/compiled/app.js" in 39.752 seconds.
Figwheel: Starting CSS Watcher for paths  ["resources/public/css"]
Launching ClojureScript REPL for build: dev
Figwheel Controls:
          (stop-autobuild)                ;; stops Figwheel autobuilder
          (start-autobuild id ...)        ;; starts autobuilder focused on optional ids
          (switch-to-build id ...)        ;; switches autobuilder to different build
          (reset-autobuild)               ;; stops, cleans, and starts autobuilder
          (reload-config)                 ;; reloads build config and resets autobuild
          (build-once id ...)             ;; builds source one time
          (clean-builds id ..)            ;; deletes compiled cljs target files
          (print-config id ...)           ;; prints out build configurations
          (fig-status)                    ;; displays current state of system
          (figwheel.client/set-autoload false)    ;; will turn autoloading off
          (figwheel.client/set-repl-pprint false) ;; will turn pretty printing off
  Switch REPL build focus:
          :cljs/quit                      ;; allows you to switch REPL to another build
    Docs: (doc function-name-here)
    Exit: :cljs/quit
 Results: Stored in vars *1, *2, *3, *e holds last exception object
Prompt will show when Figwheel connects to your application
[Rebel readline] Type :repl/help for online help info
ClojureScript 1.10.238
dev:cljs.user=>
```

* Back-end:

```
$ lein repl
Retrieving cider/piggieback/0.4.0/piggieback-0.4.0.pom from clojars
Retrieving cider/piggieback/0.4.0/piggieback-0.4.0.jar from clojars

nREPL server started on port 60366 on host 127.0.0.1 - nrepl://127.0.0.1:60366
REPL-y 0.4.3, nREPL 0.6.0
Clojure 1.9.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_192-b12
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

user=> (start)
:started
user=> 
```
The application will be autommatically served in the last window of the browser you have last used.

### Shutting Down ###

In the corresponding shell sessions mentioned in the previous section:

* Front-end:

```
dev:cljs.user=> :cljs/quit
$
```

* Back-end:

```
user=> (stop)
user=> (quit)
Bye for now!
```

## Usage ##

The only non obvious functionalities are the ones implemented by means of GraphQL subscriptions: as a new link is submitted, or one already existing is voted for, those events are notified to every client.

You can replicate these cases by means of GraphiQL, the GraphQL IDE supplied out of the box with Lacinia Pedestal, mentioned above, in the Setup section. Accessing it in a different tab, you can get the token mentioned there for configuration as you signup a new user, or login, in your browser's developer tools -> Application -> Local Storage -> "token" entry.

A couple seconds after running each of these mutations, the new link or vote will appear in the Hacker News application.

GraphQL mutations:

New Link:

```graphql
mutation post($url:String!, $description:String!) {
    post(
      url: $url,
      description: $description
    ) {
      id
    }
  }
```

Parameters:

```json
{
  "url": "https://simulacrum.party/posts/the-mutable-web/",
  "description": "The Mutable Web"
}
```

Vote:

```graphql
mutation vote($link_id:ID!) {
    vote(
      link_id: $link_id
    ) {
      id
    }
  }
```
  
Parameter:

```json
{
  "link_id": 2
}
```

[re-graph]: https://github.com/oliyh/re-graph "re-graph"

[re-graph-issue1]: https://github.com/oliyh/re-graph/issues/42 "Subscription to Lacinia Pedestal back end: Getting just the first event"

[re-graph-issue2]: https://github.com/oliyh/re-graph/issues/48 "Queries and Mutations: websockets or HTTP?"

[re-graph-fork]: https://github.com/promesante/re-graph "re-graph fork"
