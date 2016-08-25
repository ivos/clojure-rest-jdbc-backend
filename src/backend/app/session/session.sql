-- :name list-active-sessions :? :*
select
  s.*,
  u.*
from session as s
  join user as u on s.user = u.id
where expires > now();

-- :name read-session :? :1
select *
from session
where token = :token;
