drop table if exists local.authors;
create table local.authors (
  au_id varchar(11) not null PRIMARY KEY,
  au_lname varchar(40) not null,
  au_fname varchar(20) not null,
  phone char(12) null,
  address varchar (40) null,
  city varchar(20) null,
  state char(2) null,
  zip char(5) null
  );
grant select on local.authors to public;
