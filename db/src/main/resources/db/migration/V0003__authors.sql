drop table if exists authors;
create table authors (
  id char(36) not null PRIMARY KEY,
  last_name varchar(40) not null,
  first_name varchar(20) null,
  location_id char(36) references locations(id)
  );
grant select on authors to public;
