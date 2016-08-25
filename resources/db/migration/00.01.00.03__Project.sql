create table project (
  id               bigint primary key auto_increment,
  version          bigint        not null,
  code             varchar(100)  not null,
  name             varchar2(100) not null,
  visibility       varchar(16)   not null,
  description      varchar2(500),
  start            date,
  duration         integer,
  budget           decimal(10, 3),
  daily_meeting_at time,
  kick_off         datetime,
  created          timestamp     not null
);

alter table project
  add constraint ck_project_visibility check visibility in ('public', 'private');
