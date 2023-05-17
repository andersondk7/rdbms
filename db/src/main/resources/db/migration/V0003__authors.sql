drop table if exists authors;
create table authors (
  id char(36) not null PRIMARY KEY,
  version int not null default 1,
  last_name varchar(40) not null,
  first_name varchar(20) null,
  location_id char(36) references locations(id),
  create_date TIMESTAMP not null default CURRENT_TIMESTAMP,
  update_date TIMESTAMP
  );
grant select on authors to public;
