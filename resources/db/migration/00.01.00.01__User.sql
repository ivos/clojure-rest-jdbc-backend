create table user (
  id bigint primary key auto_increment,
  version bigint not null,
  username varchar(100) not null,
  email varchar2(100) not null,
  name varchar2(100) not null,
  password_hash varchar(100) not null,
  status varchar(16) not null
);

alter table user add constraint user_status_ck check status in ('active','disabled');

create unique index user_username_ui on user (username);
create unique index user_email_ui on user (email);
