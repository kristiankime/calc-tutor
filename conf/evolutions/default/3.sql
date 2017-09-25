# --- !Ups

CREATE TABLE organization
(
  id SERIAL NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL,
  creation_date TIMESTAMP NOT NULL,
  update_date TIMESTAMP NOT NULL
);

CREATE TABLE course
(
  id SERIAL NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL,
  organization_id INT NOT NULL REFERENCES organization(id),
  owner_id INT NOT NULL REFERENCES app_user(id),
  edit_code VARCHAR NOT NULL,
  view_code VARCHAR,
  creation_date TIMESTAMP NOT NULL,
  update_date TIMESTAMP NOT NULL
);

CREATE TABLE app_user_2_course
(
  user_id INT NOT NULL REFERENCES app_user(id),
  course_id INT NOT NULL REFERENCES course(id),
  access SMALLINT NOT NULL

);

# --- !Downs

DROP TABLE app_user_2_course CASCADE;
DROP TABLE course CASCADE;
DROP TABLE organization CASCADE;
