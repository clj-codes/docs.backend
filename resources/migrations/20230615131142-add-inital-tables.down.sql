DROP TRIGGER IF EXISTS set_note_updated_at on article;

--;;

DROP INDEX note_definition_id_idx;

--;;

DROP TABLE IF EXISTS note;

--;;

DROP TABLE IF EXISTS example_edit;

--;;

DROP INDEX example_definition_id_idx;

--;;

DROP TABLE IF EXISTS example;

--;;

DROP INDEX see_also_definition_id_idx;

--;;

DROP TABLE IF EXISTS see_also;

--;;

DROP TABLE IF EXISTS author;

--;;

DROP FUNCTION IF EXISTS trigger_set_timestamp();

--;;

DROP EXTENSION IF EXISTS "uuid-ossp";
