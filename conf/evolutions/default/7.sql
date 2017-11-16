# --- !Ups

CREATE TABLE skill
(
  id SERIAL NOT NULL,
  name VARCHAR NOT NULL PRIMARY KEY,
  short_name VARCHAR NOT NULL,
  intercept DOUBLE PRECISION NOT NULL,
  correct DOUBLE PRECISION NOT NULL,
  incorrect DOUBLE PRECISION NOT NULL
);

CREATE UNIQUE INDEX skill_idx__id ON skill(id);
CREATE UNIQUE INDEX skill_idx__short_name ON skill(short_name);

CREATE TABLE skill_2_question
(
  skill_id INT NOT NULL REFERENCES skill(id),
  question_id INT NOT NULL REFERENCES question(id),
  PRIMARY KEY(skill_id, question_id)
);

CREATE TABLE user_answer_count
(
  user_id INT NOT NULL REFERENCES app_user(id),
  skill_id INT NOT NULL REFERENCES skill(id),
  correct INT NOT NULL,
  incorrect INT NOT NULL,
  PRIMARY KEY(user_id, skill_id)
);

# --- !Downs

DROP TABLE user_answer_count CASCADE;
DROP TABLE skill_2_question CASCADE;
DROP TABLE skill CASCADE;
