drop table if exists countries;
create table countries (
  id char(36) not null PRIMARY KEY,
  version int not null default 1,
  country_name varchar(40) not null,
  country_abbreviation varchar(5) not null,
  create_date TIMESTAMP not null default CURRENT_TIMESTAMP,
  update_date TIMESTAMP
  );
grant select on countries to public;
