drop table if exists local.authors_titles;
create table local.authors_titles (
  author_id char(36) references authors(id) not null,
  title_id char(36) references titles(id) not null,
  author_order int default 1
  );
grant select on local.authors_titles to public;
