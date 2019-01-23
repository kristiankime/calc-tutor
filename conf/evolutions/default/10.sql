# --- !Ups


ALTER TABLE quiz ADD COLUMN description_raw VARCHAR NOT NULL DEFAULT '';
ALTER TABLE quiz ADD COLUMN description_html VARCHAR NOT NULL DEFAULT '';

# --- !Downs

ALTER TABLE table_name
DROP COLUMN description_raw CASCADE,
DROP COLUMN description_html CASCADE;

