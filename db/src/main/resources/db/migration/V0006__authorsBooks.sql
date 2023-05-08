drop table if exists local.authors_books;
create table local.authors_books (
  author_id char(36) references authors(id) not null,
  book_id char(36) references books(id) not null,
  author_order int default 1
  );
grant select on local.authors_books to public;
