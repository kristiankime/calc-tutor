# --- !Ups


ALTER TABLE question
ADD archived SMALLINT DEFAULT 0 NOT NULL;

# --- !Downs

ALTER TABLE question
DROP COLUMN archived;
