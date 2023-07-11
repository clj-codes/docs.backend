CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--;;

CREATE TABLE IF NOT EXISTS authors (
  author_id uuid UNIQUE NOT NULL PRIMARY KEY,
  login VARCHAR(150) NOT NULL,
  account_source VARCHAR(150) NOT NULL,
  avatar_url VARCHAR(500) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (login, account_source)
);

--;;

CREATE TABLE IF NOT EXISTS see_alsos (
  see_also_id uuid UNIQUE NOT NULL PRIMARY KEY,
  author_id uuid NOT NULL,
  definition_id VARCHAR(500) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT author_see_also_fk FOREIGN KEY(author_id) REFERENCES authors(author_id)
);

--;;

CREATE TABLE IF NOT EXISTS examples (
  example_id uuid UNIQUE NOT NULL PRIMARY KEY,
  definition_id VARCHAR(500) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE
);

--;;

CREATE TABLE examples_authors (
  example_id uuid REFERENCES examples(example_id) ON UPDATE CASCADE ON DELETE CASCADE,
  author_id uuid REFERENCES authors(author_id) ON UPDATE CASCADE,
  body TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT example_author_fk FOREIGN KEY(author_id) REFERENCES authors(author_id),
  CONSTRAINT example_body_fk FOREIGN KEY(example_id) REFERENCES examples(example_id)
);

--;;

CREATE TABLE IF NOT EXISTS notes (
  note_id uuid UNIQUE NOT NULL PRIMARY KEY,
  author_id uuid NOT NULL,
  definition_id VARCHAR(500) NOT NULL,
  body TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE,
  CONSTRAINT author_note_fk FOREIGN KEY(author_id) REFERENCES authors(author_id)
);
