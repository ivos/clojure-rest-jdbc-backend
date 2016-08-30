alter table user
  add roles varchar(100);

update user
set roles = 'user';

alter table user
  alter column roles varchar(100) not null;
