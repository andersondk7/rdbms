drop table if exists authors_books;
create table authors_books (
  author_id char(36) references authors(id) not null,
  book_id char(36) references books(id) not null,
  author_order int default 1
  );
grant select on authors_books to public;
