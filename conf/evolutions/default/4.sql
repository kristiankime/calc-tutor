# --- !Ups

CREATE TABLE quiz
(
  id SERIAL NOT NULL PRIMARY KEY,
  owner_id INT NOT NULL REFERENCES app_user(id),
  name VARCHAR NOT NULL,
  creation_date TIMESTAMP NOT NULL,
  update_date TIMESTAMP NOT NULL
);

CREATE TABLE app_user_2_quiz
(
  user_id INT NOT NULL REFERENCES app_user(id),
  quiz_id INT NOT NULL REFERENCES quiz(id),
  access SMALLINT NOT NULL,
  PRIMARY KEY (user_id, quiz_id)
);

CREATE TABLE course_2_quiz
(
  course_id INT NOT NULL REFERENCES course(id),
  quiz_id INT NOT NULL REFERENCES quiz(id),
  start_date TIMESTAMP,
  end_date TIMESTAMP,
  PRIMARY KEY (course_id, quiz_id)
);
# --- !Downs

DROP TABLE course_2_quiz CASCADE;
DROP TABLE app_user_2_quiz CASCADE;
DROP TABLE quiz CASCADE;

