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
  CONSTRAINT fk_author_see_also
    FOREIGN KEY(author_id)
      REFERENCES authors(author_id)
);

--;;

CREATE TABLE IF NOT EXISTS examples (
  example_id uuid UNIQUE NOT NULL PRIMARY KEY,
  author_id uuid NOT NULL,
  definition_id VARCHAR(500) NOT NULL,
  body TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE,
  CONSTRAINT fk_author_see_also
    FOREIGN KEY(author_id)
      REFERENCES authors(author_id)
);

--;;

CREATE TABLE examples_editors (
  example_id int REFERENCES examples(example_id) ON UPDATE CASCADE ON DELETE CASCADE,
  author_id int REFERENCES authors(author_id) ON UPDATE CASCADE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT examples_editors_pkey PRIMARY KEY (example_id, author_id)
);
