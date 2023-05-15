drop table if exists publishers;
create table publishers (
  id char(36) not null PRIMARY KEY,
  version int not null default 1,
  publisher_name varchar(40) not null,
  location_id char(36) references locations(id),
  website varchar(60) null,
  create_date TIMESTAMP not null default CURRENT_DATE,
  update_date TIMESTAMP
  );
grant select on publishers to public;
