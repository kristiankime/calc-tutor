# --- !Ups

CREATE TABLE question_uc_integer
(
  id SERIAL NOT NULL PRIMARY KEY,
  question_id INT NOT NULL REFERENCES question(id),
  lower INT NOT NULL,
  upper INT NOT NULL
);

CREATE TABLE question_uc_decimal
(
  id SERIAL NOT NULL PRIMARY KEY,
  question_id INT NOT NULL REFERENCES question(id),
  lower DOUBLE NOT NULL,
  upper DOUBLE NOT NULL,
  precision INT NOT NULL
);

CREATE TABLE question_uc_set
(
  id SERIAL NOT NULL PRIMARY KEY,
  question_id INT NOT NULL REFERENCES question(id),
  values_raw VARCHAR NOT NULL,
  values_math VARCHAR NOT NULL
);


CREATE UNIQUE INDEX question_uc_integer_idx__question_id ON question_uc_integer(question_id);
CREATE UNIQUE INDEX question_uc_decimal_idx__question_id ON question_uc_decimal(question_id);
CREATE UNIQUE INDEX question_uc_set_idx__question_id ON question_uc_set(question_id);

# --- !Downs

DROP TABLE question_uc_set CASCADE;
DROP TABLE question_uc_decimal CASCADE;
DROP TABLE question_uc_integer CASCADE;
