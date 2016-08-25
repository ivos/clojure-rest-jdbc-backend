-- :name list-all-users :? :*
select *
from user;

-- :name read-user :? :1
select *
from user
where (username = :username)
      or (email = :username);

-- :name expand-users :? :*
select *
from user
where id in (:v*:ids);
