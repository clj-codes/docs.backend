DROP TRIGGER IF EXISTS set_note_updated_at on article;

--;;

DROP TABLE IF EXISTS note;

--;;

DROP TABLE IF EXISTS example_edit;

--;;

DROP TABLE IF EXISTS example;

--;;

DROP TABLE IF EXISTS see_also;

--;;

DROP TABLE IF EXISTS author;

--;;

DROP FUNCTION IF EXISTS trigger_set_timestamp();

--;;

DROP EXTENSION IF EXISTS "uuid-ossp";
