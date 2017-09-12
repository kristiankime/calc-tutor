# --- !Ups



CREATE TABLE answer
(
  id SERIAL NOT NULL PRIMARY KEY,
  owner_id INT NOT NULL REFERENCES app_user(id),
  question_id INT NOT NULL REFERENCES question(id),
  all_correct SMALLINT NOT NULL,
  creation_date TIMESTAMP NOT NULL
);

CREATE TABLE answer_section
(
  id SERIAL NOT NULL PRIMARY KEY,
  answer_id INT NOT NULL REFERENCES answer(id),
  question_section_id INT NOT NULL REFERENCES question_section(id),
  question_id INT NOT NULL REFERENCES question(id),
  choice SMALLINT,
  correct SMALLINT NOT NULL,
  section_order SMALLINT NOT NULL
);

CREATE TABLE answer_part
(
  id SERIAL NOT NULL PRIMARY KEY,
  answer_section_id INT NOT NULL REFERENCES answer_section(id),
  answer_id INT NOT NULL REFERENCES answer(id),
  question_part_id INT NOT NULL REFERENCES question_part_function(id),
  question_section_id INT NOT NULL REFERENCES question_section(id),
  question_id INT NOT NULL REFERENCES question(id),
  function_raw VARCHAR NOT NULL,
  function_math VARCHAR NOT NULL,
  correct SMALLINT NOT NULL,
  part_order SMALLINT NOT NULL
);







# --- !Downs

DROP TABLE question_part_choice CASCADE;
DROP TABLE answer_section CASCADE;
DROP TABLE answer CASCADE;

