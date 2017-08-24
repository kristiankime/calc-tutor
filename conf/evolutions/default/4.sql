# --- !Ups

CREATE TABLE quiz
(
  id SERIAL NOT NULL PRIMARY KEY,
  owner_id INT NOT NULL REFERENCES app_user(id),
  name VARCHAR NOT NULL,
  creation_date TIMESTAMP,
  update_date TIMESTAMP
);

CREATE TABLE question
(
  id SERIAL NOT NULL PRIMARY KEY,
  owner_id INT NOT NULL REFERENCES app_user(id),
  title VARCHAR NOT NULL,
  description_raw VARCHAR NOT NULL,
  description_html VARCHAR NOT NULL,
  creation_date TIMESTAMP,
  update_date TIMESTAMP
);

CREATE TABLE question_section
(
  id SERIAL NOT NULL PRIMARY KEY,
  questionId INT NOT NULL REFERENCES question(id),
  description_raw VARCHAR NOT NULL,
  description_html VARCHAR NOT NULL,
  order SMALLINT
);

CREATE TABLE question_part_choice
(
  id SERIAL NOT NULL PRIMARY KEY,
  sectionId INT NOT NULL REFERENCES section(id),
  questionId INT NOT NULL REFERENCES question(id),
  explanation_raw VARCHAR NOT NULL,
  explanation_html VARCHAR NOT NULL,
  correct_choice SMALLINT,
  order SMALLINT
);

CREATE TABLE question_part_function
(
  id SERIAL NOT NULL PRIMARY KEY,
  sectionId INT NOT NULL REFERENCES section(id),
  questionId INT NOT NULL REFERENCES question(id),
  explanation_raw VARCHAR NOT NULL,
  explanation_html VARCHAR NOT NULL,
  function_raw VARCHAR NOT NULL,
  function_math VARCHAR NOT NULL,
  order SMALLINT
);

# --- !Downs

DROP TABLE course CASCADE;
DROP TABLE question CASCADE;
DROP TABLE question_section CASCADE;