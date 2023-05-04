drop table if exists local.publishers;
create table local.publishers (
  id char(36) not null PRIMARY KEY,
  publisher_name varchar(40) not null,
  location_id varchar(36) null,
  website varchar(60) null
  );
grant select on local.publishers to public;
