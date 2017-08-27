# --- !Ups

CREATE TABLE question
(
  id SERIAL NOT NULL PRIMARY KEY,
  owner_id INT NOT NULL REFERENCES app_user(id),
  title VARCHAR NOT NULL,
  description_raw VARCHAR NOT NULL,
  description_html VARCHAR NOT NULL,
  creation_date TIMESTAMP NOT NULL
);

CREATE TABLE question_section
(
  id SERIAL NOT NULL PRIMARY KEY,
  question_id INT NOT NULL REFERENCES question(id),
  description_raw VARCHAR NOT NULL,
  description_html VARCHAR NOT NULL,
  section_order SMALLINT NOT NULL
);

CREATE TABLE question_part_choice
(
  id SERIAL NOT NULL PRIMARY KEY,
  section_id INT NOT NULL REFERENCES question_section(id),
  question_id INT NOT NULL REFERENCES question(id),
  explanation_raw VARCHAR NOT NULL,
  explanation_html VARCHAR NOT NULL,
  correct_choice SMALLINT NOT NULL,
  part_order SMALLINT NOT NULL
);

CREATE TABLE question_part_function
(
  id SERIAL NOT NULL PRIMARY KEY,
  section_id INT NOT NULL REFERENCES question_section(id),
  question_id INT NOT NULL REFERENCES question(id),
  explanation_raw VARCHAR NOT NULL,
  explanation_html VARCHAR NOT NULL,
  function_raw VARCHAR NOT NULL,
  function_math VARCHAR NOT NULL,
  part_order SMALLINT NOT NULL
);

# --- !Downs

DROP TABLE course_2_quiz CASCADE;
DROP TABLE question_part_function CASCADE;
DROP TABLE question_part_choice CASCADE;
DROP TABLE question_section CASCADE;
DROP TABLE question_section CASCADE;
DROP TABLE question CASCADE;
DROP TABLE course CASCADE;

