drop table if exists local.authors_titles;
create table local.authors_titles (
  title_id char(36) not null,
  author_id char(36) not null
  );
grant select on local.authors_titles to public;
