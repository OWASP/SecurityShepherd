# Troubleshooting

This page is for operators of a Docker installation. It covers the problems people run into most often, with the container names from the default `.env`.

## Where the logs are

```bash
# Tomcat's console, including Security Shepherd's log messages
docker logs secshep_tomcat

# Security Shepherd's own log, and Tomcat's logs, in the logs volume
docker exec secshep_tomcat ls /usr/local/tomcat/logs
docker exec secshep_tomcat tail -n 100 /usr/local/tomcat/logs/security-shepherd.log
```

Errors thrown while handling a request are in `localhost.<date>.log`.

## Installation and setup

### The Maven build fails on a newer JDK

The build stops at its formatting check with an error like this:

```text
google-java-format(jvm-version) You are running Spotless on JVM 25.
```

`mvn install`, `mvn verify` and `mvn spotless:check` need JDK 17, and this happens on JDK 25 or newer. To run Security Shepherd without changing its code, build with `mvn -Pdocker clean package -DskipTests`, which stops before the check. To contribute, use JDK 17, as the [prerequisites](getting-started.md#prerequisites) explain.

### The browser warns about the certificate

A new installation uses a self-signed certificate, so browsers warn about it. Accept the warning to continue, or install your own certificate as described in [Deploying into production](deploying-into-production.md#use-your-own-tls-certificate).

### Port 80 or 443 is already in use

Another program on the host is using the port. Set `HTTP_PORT` and `HTTPS_PORT` in `.env` to free ports, such as `8080` and `8443`, and run `docker compose up -d`. Then open `https://localhost:8443`.

### The setup page asks for an authentication token

The token proves you can reach the server's files. Get it from the Tomcat container:

```bash
docker exec secshep_tomcat cat /usr/local/tomcat/conf/SecurityShepherd.auth
```

Once setup succeeds, the token file is deleted, the setup page redirects to the login page, and the setup form can't be submitted again.

### Setup can't connect to the database

Tomcat reaches the databases over the Docker network, so the hostnames on the setup page must be the container names, such as `secshep_mariadb` and `secshep_mongo`, not `localhost`. The password is `DB_PASS` from `.env`, unless you've changed it since the `data` volume was created.

### Changing the database settings after setup

The setup page only runs once. To point Security Shepherd at a different database server or password afterwards, edit `database.properties` in the `conf` volume, as described in [Configuration](configuration.md#databaseproperties), and restart Tomcat. To start again with an empty database, see [Start again from scratch](backups.md#start-again-from-scratch).

Deleting `database.properties` to get the setup page back doesn't work at the moment: setup then fails with `No suitable driver found` ([#1082](https://github.com/OWASP/SecurityShepherd/issues/1082)).

### Scripted setup fails with "Server Error"

If you run setup with `curl` rather than the form, send every field the form sends, including `unsafeLevels` and `enableMongoChallenge`. Without them, setup fails part-way through ([#956](https://github.com/OWASP/SecurityShepherd/issues/956)). Use `disable` for a level you don't want:

```bash
curl -k -s https://localhost/setup \
  --data-urlencode "dbhost=secshep_mariadb" --data-urlencode "dbport=3306" \
  --data-urlencode "dbuser=root" --data-urlencode "dbpass=$DB_PASS" \
  --data-urlencode "dboverride=override" --data-urlencode "dbauth=$AUTH_TOKEN" \
  --data-urlencode "mhost=secshep_mongo" --data-urlencode "mport=27017" \
  --data-urlencode "enableMongoChallenge=enable" --data-urlencode "unsafeLevels=disable"
```

### The MariaDB container exits with a SQL syntax error

If the MariaDB container stops on its first start with an error like this:

```text
ERROR 1064 (42000) at line 181: You have an error in your SQL syntax;
check the manual that corresponds to your MariaDB server version for the right syntax to use near '' at line 3
```

the image was built from SQL scripts whose `DELIMITER` lines were never uncommented. The SQL files in `src/main/resources/database/` have their `DELIMITER` statements commented out, and `mvn -Pdocker` uncomments them in the copies under `docker/mariadb/target/`. That happens in the `generate-sources` phase, so the scripts aren't converted if they were generated with `mvn -Pdocker validate` alone, or if Docker reused an image built before the conversion ran.

```bash
mvn -Pdocker generate-sources            # convert the SQL scripts (validate alone doesn't)
docker compose build --no-cache db       # rebuild without the cache
docker compose down -v                   # remove the volumes left by the failed start
docker compose up -d db
```

`docker compose down -v` deletes every volume, including any data. It's needed because MariaDB only runs its setup scripts on an empty `data` volume.

### `mariadb` says "SSL is required, but the server does not support it"

The `mariadb` client in the MariaDB 12.3 image expects TLS by default, and the server doesn't offer it inside the container. Add `--skip-ssl`:

```bash
docker exec -it secshep_mariadb mariadb --skip-ssl -uroot -p core
```

## During an event

### Players don't see a change

Most admin changes take effect immediately, but players have to reload the page to see them. For the registration link, they reload the login page.

### A level isn't in a player's menu

Check that the level is open, under **Admin → Module Management → Open and Close Modules**. In CTF Mode, players see only their next level. If a module block is set, players can't go past the blocked level.

### No one can open a level or submit a key

If [event times](configuration.md#event-times) are set, the levels are only available between `startTime` and `endTime`, in the server's time zone (`TZ`). Check them with:

```bash
docker exec -it secshep_mariadb mariadb --skip-ssl -uroot -p core -e 'SELECT * FROM settings'
```

### Players get no points for completing levels

After `lockTime`, completions are recorded but score nothing, so the scoreboard is frozen. See [Event times](configuration.md#event-times).

### The NoSQL injection level doesn't work

The level needs MongoDB. Check that the `secshep_mongo` container is running (`docker compose ps`), and that `mongo.properties` in the `conf` volume points at it, as described in [Configuration](configuration.md#mongoproperties). Turn the level on with **Enable NoSQL Challenge** when you run setup.

### Errors under load: "Connection is not available"

The database connection pool is exhausted. Raise `pool.maximumPoolSize` in `database.properties`, as described in [Configuration](configuration.md#databaseproperties), and restart Tomcat.

### A database change has no effect

Security Shepherd keeps its settings in memory. After changing the `settings` table directly, restart Tomcat with `docker compose restart web`.
