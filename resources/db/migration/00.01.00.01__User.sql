create table user (
  id            bigint primary key auto_increment,
  version       bigint        not null,
  username      varchar(100)  not null,
  email         varchar2(100) not null,
  name          varchar2(100) not null,
  password_hash varchar(100)  not null,
  status        varchar(16)   not null
);

alter table user
  add constraint ck_user_status check status in ('active', 'disabled');

create unique index ui_user_username
  on user (username);
create unique index ui_user_email
  on user (email);
