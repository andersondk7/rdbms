drop table if exists local.titles;
create table local.titles (
  id char(36) not null PRIMARY KEY,
  name varchar(200) not null,
  price dec(5,2) not null,
  publisher_id char(36) references publishers(id)
  );
grant select on local.titles to public;
