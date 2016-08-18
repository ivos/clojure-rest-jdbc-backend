-- :name list-all-projects :? :*
select * from project;

-- :name read-project :? :1
select * from project where code = :code;
