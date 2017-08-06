# Loing/Users schema for pac4j http://www.pac4j.org/2.0.x/docs/authenticators/sql.html

# --- !Ups

CREATE TABLE app_user
(
  id VARCHAR PRIMARY KEY NOT NULL,
  name VARCHAR NOT NULL,
  email VARCHAR
);

# --- !Downs

DROP TABLE app_user;