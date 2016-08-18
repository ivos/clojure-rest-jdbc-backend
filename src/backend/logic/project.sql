-- :name list-all-projects :? :*
select * from project;

-- :name get-project :? :1
select * from project where id = :id;
