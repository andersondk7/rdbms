drop table if exists locations;
create table locations (
  id char(36) not null PRIMARY KEY,
  version int not null default 1,
  location_name varchar(40) not null,
  location_abbreviation varchar(4) not null,
  country_id char(36) references countries(id),
  create_date TIMESTAMP not null default CURRENT_TIMESTAMP,
  update_date TIMESTAMP
  );
grant select on locations to public;
