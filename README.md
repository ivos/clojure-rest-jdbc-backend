# Clojure REST + JDBC backend

Sample of a REST backend on JDBC written in Clojure.

## Usage

### Development

#### Database

Migrate database (bring DB structure up-to-date):

    lein db-migrate

Clean database:

    lein db-clean

#### Application

1. Start application via Leiningen

		lein ring server-headless

	Changes to source code are now automatically picked-up by the running application.

2. Invoke application:

		curl -i -X POST -H "Content-Type: application/json" -d '{
		    "code" : "code-1",
		    "name" : "Name 1",
		    "visibility" : "private"
		}' "http://localhost:3000/projects"

### Production

1. Build and package for production

		lein uberjar

2. Run in production

		java -jar target/uberjar/clojure-rest-jdbc-backend-*-standalone.jar

## License

Copyright © 2016 Ivo Maixner

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
