#!/usr/bin/env bash

dropdb --if-exists hndb
createdb hndb

dropuser --if-exists hn_role
psql hndb -a  <<__END
create user hn_role password 'lacinia';
__END

psql -Uhn_role hndb -f setup-db.sql
