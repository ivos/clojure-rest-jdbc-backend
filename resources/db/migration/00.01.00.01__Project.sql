create table project (
  id bigint primary key auto_increment,
  version bigint not null,
  name varchar2(100) not null,
  visibility varchar(16) not null,
  created date not null
);

alter table project add constraint project_visibility_ck check visibility in ('public','private');
