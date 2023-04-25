drop table if exists local.publishers;
create table local.publishers (
  pub_id varchar(11) not null PRIMARY KEY,
  pub_name varchar(40) not null,
  address varchar (40) null,
  city varchar(20) null,
  state char(2) null,
  zip char(5) null
  );
grant select on local.publishers to public;
