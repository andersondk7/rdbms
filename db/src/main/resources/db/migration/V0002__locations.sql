drop table if exists local.locations;
create table local.locations (
  id char(36) not null PRIMARY KEY,
  location_name varchar(40) not null,
  location_abbreviation varchar(4) not null,
  country_id char(36) references countries(id)
  );
grant select on local.locations to public;
