# Loing/Users schema for pac4j http://www.pac4j.org/2.0.x/docs/authenticators/sql.html

# --- !Ups

CREATE TABLE organization
(
  id SERIAL NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL,
  creation_date TIMESTAMP,
  update_date TIMESTAMP
);

CREATE TABLE course
(
  id SERIAL NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL,
  organization_id INT NOT NULL REFERENCES organization(id),
  owner_id INT NOT NULL REFERENCES app_user(id),
  edit_code VARCHAR NOT NULL,
  view_code VARCHAR,
  creation_date TIMESTAMP,
  update_date TIMESTAMP
);

# --- !Downs

DROP TABLE course CASCADE;
DROP TABLE organization CASCADE;