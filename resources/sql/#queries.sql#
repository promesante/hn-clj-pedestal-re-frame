
-- name: list-links
select id, description, url, usr_id, created_at, updated_at
from link

-- name: filter-links
select id, description, url, usr_id, created_at, updated_at
from link
where description like :filter
or url like :filter
limit ?::integer
offset ?::integer

-- name: filter-links-count
select count(*)
from link
where description like :filter
or url like :filter

-- name: filter-links-order
select id, description, url, usr_id, created_at, updated_at
from link
where description like :filter
or url like :filter
limit ?::integer
offset ?::integer
order by :field :criteria

-- name: find-link-by-id
select id, description, url, usr_id, created_at, updated_at
from link
where id = ?::integer

-- name: find-user-by-id
select id, name, email, password, created_at, updated_at
from usr
where id = ?::integer

-- name: find-user-by-email
select id, name, email, password, created_at, updated_at
from usr
where email = :email

-- name: find-votes-by-link-usr
select id
from vote
where link_id = ?::integer
and usr_id = ?::integer

-- name: find-user-by-link
select u.id, u.name, u.email, u.password, u.created_at, u.updated_at
from link l
inner join usr u
on (l.usr_id = u.id)
where l.id = ?::integer

-- name: find-links-by-user
select l.id, l.description, l.url, l.usr_id, l.created_at, l.updated_at
from link l
inner join usr u
on (l.usr_id = u.id)
where u.id = ?::integer

-- name: find-votes-by-link
select
v.id,
u.id as usr_id, u.name, u.email, u.password,
u.created_at, u.updated_at
from vote v
inner join link l on (v.link_id = l.id)
inner join usr u on (v.usr_id = u.id)
where l.id = ?::integer

-- name: insert-link<!
insert into link (description, url, usr_id)
values (:description, :url, ?::integer)

-- name: insert-user<!
insert into usr (email, password, name)
values (:email, :password, :name)

-- name: insert-vote<!
insert into vote (link_id, usr_id)
values (?::integer, ?::integer)
