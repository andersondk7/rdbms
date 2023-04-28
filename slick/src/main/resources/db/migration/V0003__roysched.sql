drop table if exists local.roysched;
create table local.roysched (
  title_id varchar(11) not null PRIMARY KEY,
  lorange int null,
  hirange int null,
  royalty dec(5,2) null
  );
grant select on local.roysched to public;
