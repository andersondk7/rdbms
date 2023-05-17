drop table if exists books;
create table books (
  id char(36) not null PRIMARY KEY,
  version int not null default 1,
  title varchar(200) not null,
  price dec(5,2) not null,
  publisher_id char(36) references publishers(id),
  publish_date DATE,
  create_date TIMESTAMP not null default CURRENT_TIMESTAMP,
  update_date TIMESTAMP
  );
grant select on books to public;
