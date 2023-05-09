drop table if exists locations;
create table locations (
  id char(36) not null PRIMARY KEY,
  location_name varchar(40) not null,
  location_abbreviation varchar(4) not null,
  country_id char(36) references countries(id)
  );
grant select on locations to public;
