drop table if exists publishers;
create table publishers (
  id char(36) not null PRIMARY KEY,
  publisher_name varchar(40) not null,
  location_id char(36) references locations(id),
  website varchar(60) null
  );
grant select on publishers to public;
