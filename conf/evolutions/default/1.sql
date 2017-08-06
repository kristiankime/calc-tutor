# Login/Users schema for pac4j http://www.pac4j.org/2.0.x/docs/authenticators/sql.html

# --- !Ups

CREATE TABLE name_pass_login
(
    id VARCHAR PRIMARY KEY NOT NULL,
    user_name VARCHAR NOT NULL,
    password VARCHAR NOT NULL,
    linkedid VARCHAR,
    serializedprofile VARCHAR
);

CREATE INDEX logins_idx_user_name ON name_pass_login (user_name);
CREATE INDEX logins_idx_linked_id ON name_pass_login (linkedid);

# --- !Downs

DROP TABLE name_pass_login;