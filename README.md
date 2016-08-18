# Clojure REST + JDBC backend

Sample REST backend on JDBC written in Clojure.

## Technological stack

- Language [Clojure](https://clojure.org/)
- State management [Component](https://github.com/stuartsierra/component)
- SQL access [HugSQL](http://www.hugsql.org/)
- DB connection pool [HikariCP](http://brettwooldridge.github.io/HikariCP/)
- DB migrations [Flyway](https://flywaydb.org/)
- HTTP server [Ring](https://github.com/ring-clojure/ring)
- Routes definition [Compojure](https://github.com/weavejester/compojure)
- Logging [Logback](http://logback.qos.ch/)
- Tests [Midje](https://github.com/marick/Midje)
- DB tests [LightAir](http://lightair.sourceforge.net/)

## Features

- Production deployment as a .jar or .war
- "[Reloaded](http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded)" workflow in REPL
- Comprehensive integration tests that exercise the application from the HTTP handler down to the database
- CRUDL on entities: project, user, session
- Authentication and authorization

## Usage

### Development

#### CLI

##### Database

Migrate database (bring DB structure up-to-date)

    lein db-migrate

Clean database

    lein db-clean

Cleanly re-create database

    lein db-recreate

Update LightAir DB structure (call after each change in DB tables)

    lein db-update

##### Application

1. Start application via Leiningen

		lein ring server-headless

	Changes to source code are not supported in this mode, see REPL below.

2. Invoke application

		curl -i -X POST -H "Content-Type: application/json" -d '{
		    "code" : "code-1",
		    "name" : "Name 1",
		    "visibility" : "private"
		}' "http://localhost:3000/projects"

#### REPL

##### Setup & application

1. Open REPL

        lein repl

2. Load development environment

        (dev)

3. Start system

        (go)

4. Invoke application

Reload application after changing the source code

    (reset)

Stop system

    (stop)

Start system

    (start)

Update LightAir DB structure (call after each change in DB tables)

    (db-update)

##### Database

Migrate database

    (db-migrate)

Clean database

    (db-clean)

Cleanly re-create database

    (db-recreate)

### Tests

#### CLI

Run all tests

    lein test

This includes also the integration tests so the system gets started (and stopped).

#### REPL

After the system has started and the database has been migrated

    (autotest)

### Production

#### JAR

1. Build and package for production

		lein uberjar

2. Run in production

		java -jar target/uberjar/clojure-rest-jdbc-backend-*-standalone.jar

#### WAR

Build and package for production

    lein uberwar

WAR is built at

    target/uberjar/clojure-rest-jdbc-backend-*-standalone.war

and is deployable on a default installation of Tomcat 7.0.57.

## License

Copyright © 2016 Ivo Maixner

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
