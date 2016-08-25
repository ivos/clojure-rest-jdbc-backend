create table session (
  token    char(36) primary key,
  created  timestamp not null,
  duration integer   not null,
  expires  timestamp not null,
  user     bigint    not null
);

alter table session
  add constraint fk_session_user foreign key (user) references user on delete cascade;

create index ix_session_expires
  on session (expires);
