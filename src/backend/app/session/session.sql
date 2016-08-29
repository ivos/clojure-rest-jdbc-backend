-- :name list-active-sessions :? :*
select *
from session
where expires > :now
order by expires desc, created desc, token;

-- :name read-active-session :? :1
select *
from session
where (token = :token) and (expires > :now);
