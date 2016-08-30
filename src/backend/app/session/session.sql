-- :name list-active-sessions :? :*
select *
from session
where expires > :now
order by expires desc, created desc, token;

-- :name read-active-session :? :1
select s.*
from session as s
  join user as u on s.user = u.id
where (token = :token) and (expires > :now) and (u.status = 'active');
