# hn-clj-pedestal-re-frame

Porting ["The Fullstack Tutorial for GraphQL"](https://www.howtographql.com), from Javascript to Clojure(script).

I have tried to match each of the articles in the series in the git commit sequence, and the description given to each of them.


## Setup

### Database

Database engine used is Postgresql.

Database setup is performed by setup-db.sh script in the bin directory:

```shell
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

```shell
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


### Starting Up / Shutting down ###

In two different shell sessions:

* start up front-end:

```shell
$ lein clean
$ lein figwheel
```

* start up back-end:



[re-graph]: https://github.com/oliyh/re-graph "re-graph"

[re-graph-issue1]: https://github.com/oliyh/re-graph/issues/42 "Subscription to Lacinia Pedestal back end: Getting just the first event"

[re-graph-issue2]: https://github.com/oliyh/re-graph/issues/48 "Queries and Mutations: websockets or HTTP?"

[re-graph-fork]: https://github.com/promesante/re-graph "re-graph fork"
