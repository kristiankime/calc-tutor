# Loing/Users schema for pac4j http://www.pac4j.org/2.0.x/docs/authenticators/sql.html

# --- !Ups


CREATE TABLE app_user
(
  id SERIAL NOT NULL PRIMARY KEY,
  login_id VARCHAR NOT NULL,
  name VARCHAR NOT NULL,
  email VARCHAR,
  consented BOOLEAN,
  allow_auto_match BOOLEAN,
  seen_help BOOLEAN,
  email_updates BOOLEAN,
  last_access TIMESTAMP
);

CREATE INDEX app_user_idx__login_id ON app_user(login_id);

# --- !Downs

DROP TABLE app_user CASCADE;