drop table if exists local.countries;
create table local.countries (
  id char(36) not null PRIMARY KEY,
  country_name varchar(40) not null,
  country_abbreviation varchar(5) not null
  );
grant select on local.countries to public;
