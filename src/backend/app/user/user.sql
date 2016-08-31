-- :name sql-list-all :? :*
select *
from user;

-- :name sql-read :? :1
select *
from user
where (username = :username)
      or (email = :username);

-- :name sql-expand :? :*
select *
from user
where id in (:v*:ids);
