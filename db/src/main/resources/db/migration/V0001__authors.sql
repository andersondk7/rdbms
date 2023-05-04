drop table if exists local.authors;
create table local.authors (
  id char(36) not null PRIMARY KEY,
  last_name varchar(40) not null,
  first_name varchar(20) not null,
  phone char(12) null,
  address varchar (40) null,
  city varchar(20) null,
  state char(2) null,
  zip char(5) null
  );
grant select on local.authors to public;
