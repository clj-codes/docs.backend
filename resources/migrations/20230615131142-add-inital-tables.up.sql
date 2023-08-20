CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--;;

CREATE FUNCTION trigger_set_timestamp()
  RETURNS TRIGGER AS $$
  BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
  END;
  $$ LANGUAGE plpgsql;

--;;

CREATE TABLE IF NOT EXISTS author (
  author_id uuid UNIQUE NOT NULL PRIMARY KEY DEFAULT gen_random_uuid (),
  login VARCHAR(150) NOT NULL,
  account_source VARCHAR(150) NOT NULL,
  avatar_url VARCHAR(500) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (login, account_source)
);

--;;

CREATE TABLE IF NOT EXISTS see_also (
  see_also_id uuid UNIQUE NOT NULL PRIMARY KEY DEFAULT gen_random_uuid (),
  author_id uuid NOT NULL,
  definition_id VARCHAR(500) NOT NULL,
  definition_id_to VARCHAR(500) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT author_see_also_fk FOREIGN KEY(author_id) REFERENCES author(author_id)
);

--;;

CREATE INDEX see_also_definition_id_idx ON see_also (definition_id);

--;;

CREATE TABLE IF NOT EXISTS example (
  example_id uuid UNIQUE NOT NULL PRIMARY KEY DEFAULT gen_random_uuid (),
  definition_id VARCHAR(500) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--;;

CREATE INDEX example_definition_id_idx ON example (definition_id);

--;;

CREATE TABLE example_edit (
  example_id uuid REFERENCES example(example_id) ON UPDATE CASCADE ON DELETE CASCADE,
  author_id uuid REFERENCES author(author_id) ON UPDATE CASCADE,
  body TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT example_author_fk FOREIGN KEY(author_id) REFERENCES author(author_id),
  CONSTRAINT example_edit_fk FOREIGN KEY(example_id) REFERENCES example(example_id)
);

--;;

CREATE TABLE IF NOT EXISTS note (
  note_id uuid UNIQUE NOT NULL PRIMARY KEY DEFAULT gen_random_uuid (),
  author_id uuid NOT NULL,
  definition_id VARCHAR(500) NOT NULL,
  body TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT author_note_fk FOREIGN KEY(author_id) REFERENCES author(author_id)
);

--;;

CREATE INDEX note_definition_id_idx ON note (definition_id);

--;;

CREATE TRIGGER set_note_updated_at
  BEFORE UPDATE ON note
  FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();
