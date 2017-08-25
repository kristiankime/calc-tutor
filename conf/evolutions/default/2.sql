# --- !Ups

CREATE TABLE app_user
(
  id SERIAL NOT NULL PRIMARY KEY,
  login_id VARCHAR NOT NULL,
  name VARCHAR NOT NULL,
  email VARCHAR,
  consented BOOLEAN NOT NULL,
  allow_auto_match BOOLEAN NOT NULL,
  seen_help BOOLEAN NOT NULL,
  email_updates BOOLEAN NOT NULL,
  last_access TIMESTAMP NOT NULL
);

CREATE INDEX app_user_idx__login_id ON app_user(login_id);

# --- !Downs

DROP TABLE app_user CASCADE;