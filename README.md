# codes.clj.docs.backend-malli
Backend Service for [docs.clj.codes](https://docs.clj.codes).

## Config `resources/config.edn`
This file has all the backend configurations:
- `:webserver/port` or PORT env var which port the webserver will open and expose
- `:webserver/allowed-origins` an array with allowed base domains for cors
- `:database` config to build the postgres connection string:
  - `:dbname` or DB_NAME env var
  - `:username` or DB_USER env var
  - `:password` or DB_PASS env var
  - `:host` or DB_HOST env var
  - `:port` or DB_PORT env var
- `:db-docs` coordinates to [db-docs component](src/codes/clj/docs/backend/components/db_docs.clj) downloads the latest extracted db
  - `:dir` where it will download and extract the database zip
  - `:url` base github releases url to build the download url
  - `:version` used to build the download url and set where inside `:dir` the database will be saved
  - `:file-name` used to build the download url
  - Note: Before downloading the component checks if the path `:dir`/`:version` exists and has the database,
    so you could manually copy your database files and start the server it will use this instead downloading a new one.
- `:github` where to set client-id (or GH_CLIENT_ID) and client-secret (or GH_CLIENT_SECRET) for login with github
- `:jwt` jwt encryption secret used to encrypt and decrypt the jwt returned to the frontend after login

## Project
[Check the project backlog, issues and ongoing tasks](https://github.com/orgs/clj-codes/projects/2)

## Repl
To open a nrepl
```bash
clj -M:nrepl
```
To open a nrepl with all test extra-deps on it
```bash
clj -M:dev:nrepl
```

Then, eval for instrument malli.
Malli instrumentation enables input/output validation and clj-kondo lint annotations.
```
(start)
```

## Run Tests
To run unit tests inside `./test/unit`
```bash
clj -M:dev:test :unit
```
To run integration tests inside `./test/integration`
```bash
clj -M:dev:test :integration
```
To run all tests inside `./test`
```bash
clj -M:dev:test
```
To generate a coverage report 
```bash
clj -M:dev:test --plugin kaocha.plugin/cloverage
```

## Lint fix and format

```bash
clj -M:clojure-lsp format
clj -M:clojure-lsp clean-ns
clj -M:clojure-lsp diagnostics
```

## Migrations
To create a new migration with a name
```bash
clj -M:migratus create migration-name
```
To execute all pending migrations
```bash
clj -M:migratus migrate
```
To rollback the latest migration
```bash
clj -M:migratus rollback
```
See [Migratus Usage](https://github.com/yogthos/migratus#usage) for documentation on each command.


## Docker
Start containers with postgres `user: postgres, password: postgres, hostname: db, port: 5432`  
and [pg-admin](http://localhost:5433) `email: pg@pg.cc, password: pg, port: 5433`
```bash
docker-compose -f docker/docker-compose.yml up -d
```
Stop containers
```bash
docker-compose -f docker/docker-compose.yml stop
```

## Running the server
First you need to have the database running, for this you can use the docker command in the step above.

### Repl
You can start a repl open and evaluate the file `src/microservice_boilerplate/server.clj` and execute following code:
```clojure
(start-system! (build-system-map))
```

### Uberjar
You can generate an uberjar and execute it via java in the terminal:
```bash
# genarate a target/service.jar
clj -T:build uberjar
# execute it via java
java -jar target/service.jar
```

## License
This is free and unencumbered software released into the public domain.  
For more information, please refer to <http://unlicense.org>
