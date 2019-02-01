# --- !Ups

DROP INDEX question_uc_integer_idx__question_id;
DROP INDEX question_uc_decimal_idx__question_id;
DROP INDEX question_uc_set_idx__question_id;


# --- !Downs

CREATE UNIQUE INDEX question_uc_integer_idx__question_id ON question_uc_integer(question_id);
CREATE UNIQUE INDEX question_uc_decimal_idx__question_id ON question_uc_decimal(question_id);
CREATE UNIQUE INDEX question_uc_set_idx__question_id ON question_uc_set(question_id);


