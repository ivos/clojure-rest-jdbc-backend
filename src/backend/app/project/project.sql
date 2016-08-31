-- :name sql-list-all :? :*
select *
from project;

-- :name sql-read :? :1
select *
from project
where (code = :code) and (owner = :owner);
