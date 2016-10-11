-- :name sql-list-own :? :*
select *
from project
where owner = :owner;

-- :name sql-read :? :1
select *
from project
where (code = :code) and (owner = :owner);
