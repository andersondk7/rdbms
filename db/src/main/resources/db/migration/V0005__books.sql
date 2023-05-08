drop table if exists local.books;
create table local.books (
  id char(36) not null PRIMARY KEY,
  title varchar(200) not null,
  price dec(5,2) not null,
  publisher_id char(36) references publishers(id),
  publish_date DATE
  );
grant select on local.books to public;
