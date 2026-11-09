# Configuration

This page lists every setting an operator can change: the `.env` file used by Docker Compose, the files in Tomcat's `conf` volume, and the settings stored in the database. Most of these settings are covered by the guides in Running Security Shepherd. This is the place to look one up.

## Where configuration lives

| Where | What's in it | How to change it |
| --- | --- | --- |
| `.env`, in the repository root | Image names, container names, ports, passwords, TLS settings and the time zone | Edit it, then rebuild or recreate the containers, as each setting below says |
| `conf` volume (`/usr/local/tomcat/conf`) | `database.properties`, `mongo.properties`, the TLS keystore, `server.xml` and `web.xml`, and the setup token `SecurityShepherd.auth` until setup is done | Edit the files in the volume, then restart Tomcat with `docker compose restart web` |
| `settings` table in the `core` database | Options from the Admin menu: layout, registration, scoreboard, cheat sheets, feedback, default class and the event times | The Admin menu, described in [Admin menu reference](admin-menu.md) |
| `src/main/resources/` | `sso.properties` and `onelogin.saml.properties`, for [Single sign-on](single-sign-on.md) | Edit them, then rebuild the WAR and the image |

## `.env`

Docker Compose reads `.env` for `docker-compose.yml`, and also passes every entry into the three containers as environment variables.

Some values are copied into an image or a volume when it's created, so changing them in `.env` later has no effect on its own. The **Applies** column says when a change takes effect.

| Variable | Default | What it does | Applies |
| --- | --- | --- | --- |
| `IMAGE_TOMCAT`, `IMAGE_MARIADB`, `IMAGE_MONGO` | `owasp/security-shepherd`, `owasp/security-shepherd_mariadb`, `owasp/security-shepherd_mongo` | The image names Compose builds and runs | Next `docker compose up` |
| `CONTAINER_TOMCAT`, `CONTAINER_MARIADB`, `CONTAINER_MONGO` | `secshep_tomcat`, `secshep_mariadb`, `secshep_mongo` | The container names. Tomcat reaches the databases by these names. | Image rebuild, because the Tomcat image's `database.properties` and `mongo.properties` use them |
| `TOMCAT_DOCKER_VERSION` | `11.0-jre17-temurin` | The tag of the official `tomcat` base image | Image rebuild |
| `DB_VERSION` | `12.3` | The MariaDB version | Image rebuild |
| `MONGODB_VERSION` | `4.1.13` | The MongoDB version | Image rebuild |
| `DB_USER` | `root` | The database user written into the Tomcat image's `database.properties` | Image rebuild, and only for a new `conf` volume |
| `DB_PASS` | `CowSaysMoo` | MariaDB's root password, and the password in the Tomcat image's `database.properties` | MariaDB applies it only when it creates the `data` volume, and the Tomcat image copies it when it's built. See [Change the database password](deploying-into-production.md#change-the-database-password). |
| `DB_SERVER_MAPPED_HOST`, `DB_PORT_MAPPED_HOST` | `127.0.0.1`, `3306` | The host address and port MariaDB is published on | Container recreate |
| `DB_BIND_ADDRESS` | `*` | The address MariaDB listens on inside its container | Image rebuild |
| `MONGO_SERVER_MAPPED_HOST`, `MONGO_PORT_MAPPED_HOST` | `127.0.0.1`, `27017` | The host address and port MongoDB is published on | Container recreate |
| `MONGO_BIND_ADDRESS` | `127.0.0.1` | The address MongoDB listens on inside its container | Image rebuild |
| `HTTP_PORT`, `HTTPS_PORT` | `80`, `443` | The host ports for HTTP and HTTPS. HTTP redirects to HTTPS on `HTTPS_PORT`. | Container recreate |
| `TLS_KEYSTORE_FILE` | `shepherdKeystore.p12` | The PKCS12 keystore's file name in the `conf` volume | Container recreate |
| `TLS_KEYSTORE_PASS` | `CowSaysMoo` | The keystore's password. If it's empty, a random password is generated with the keystore and kept in `conf/.tls-keystore-pass`. | Container recreate |
| `ALIAS` | `tomcat` | The key's alias in the keystore | Container recreate |
| `DNAME` | `cn=OwaspShepherd,…` | The subject of the self-signed certificate generated on the first start | Only when a keystore is generated |
| `TZ` | `Europe/Dublin` | The containers' time zone, used for log timestamps and the [event times](#event-times) | Container recreate |
| `DOCKER_NETWORK_NAME` | `securityshepherd_default` | Passed to the MariaDB image build | Image rebuild |
| `VERSION` | `3.1` | Not used | |
| `TEST_MYSQL_HOST`, `TEST_MYSQL_PORT`, `TEST_MYSQL_PASSWORD` | | Used only by the tests, see [Running tests](testing.md) | |

"Container recreate" means `docker compose up -d`, which recreates any container whose configuration changed. "Image rebuild" means `docker compose up -d --build`, after `mvn -Pdocker clean package -DskipTests` for the Tomcat image.

## `database.properties`

Tomcat's connection to MariaDB. It's in the `conf` volume, at `/usr/local/tomcat/conf/database.properties`. The image creates it from `.env` when it's built, and the setup page rewrites it.

| Property | Example | What it does |
| --- | --- | --- |
| `databaseConnectionURL` | `jdbc:mariadb://secshep_mariadb:3306/` | The JDBC URL of the server, without a database name |
| `databaseSchema` | `core` | The platform's database |
| `DriverType` | `org.mariadb.jdbc.Driver` | The JDBC driver class |
| `databaseUsername`, `databasePassword` | `root` | The account Tomcat uses for the `core` database. The levels use their own accounts, created by the setup page. |
| `databaseOptions` | `useUnicode=true&character_set_server=utf8mb4` | Added to the JDBC URL |

The connection pool (HikariCP) can be tuned with these optional properties. The defaults suit most events.

| Property | Default | What it does |
| --- | --- | --- |
| `pool.maximumPoolSize` | `20` | The most connections the pool opens |
| `pool.minimumIdle` | `5` | The fewest idle connections it keeps open |
| `pool.connectionTimeout` | `5000` | How long, in milliseconds, a request waits for a free connection before it fails |
| `pool.idleTimeout` | `600000` | How long, in milliseconds, an idle connection is kept |
| `pool.maxLifetime` | `1800000` | The longest, in milliseconds, a connection is kept |
| `pool.leakDetectionThreshold` | `60000` | Logs a warning when a connection is held for longer than this many milliseconds. `0` turns it off. |

If many players see errors at once and the log says `Connection is not available`, raise `pool.maximumPoolSize`. Each level's database account has a pool of its own, of up to three connections.

## `mongo.properties`

Tomcat's connection to MongoDB, used only by the NoSQL injection level. It's in the `conf` volume, next to `database.properties`, and the setup page writes it.

| Property | Example | What it does |
| --- | --- | --- |
| `connectionHost`, `connectionPort` | `secshep_mongo`, `27017` | The MongoDB server |
| `databaseName` | `shepherdGames` | The database the level uses |
| `connectTimeout` | `10000` | How long, in milliseconds, to wait for a connection |
| `socketTimeout` | `0` | How long, in milliseconds, to wait for a reply. `0` waits indefinitely. |
| `serverSelectionTimeout` | `30000` | How long, in milliseconds, to wait for the server to become available |
| `pool.connectionsPerHost` | `10` | Optional. The most connections to the server. |
| `pool.minConnectionsPerHost` | `2` | Optional. The fewest connections kept open. |

## Settings in the database

The Admin menu stores its options in the `settings` table of the `core` database. Change them through the Admin menu wherever you can.

Security Shepherd reads these settings once and keeps them in memory. If you change a value in the table directly, restart Tomcat (`docker compose restart web`) for it to take effect.

| Setting | Values | Changed in |
| --- | --- | --- |
| `moduleLayout` | `open`, `ctf`, `tournament` | **Module Management → Change Module Layout** |
| `openRegistration` | `1` (open) or `0` (closed) | **Configuration → Open/Close Registration** |
| `scoreboardStatus` | `closed`; `open` (logged-in users); `public` (anyone, without logging in); `classSpecific` (players see their own class); `adminOnly` | **Configuration → Scoreboard Configuration** |
| `scoreboardClass` | A class ID, to list only that class on the scoreboard, or empty for every class | **Configuration → Scoreboard Configuration** |
| `adminCheatsEnabled`, `playerCheatsEnabled` | `1` or `0` | **Configuration → Cheat Sheet Management** |
| `enableFeedback` | `1` or `0` | **Configuration → Feedback Configuration** |
| `defaultClass` | A class ID, or empty | **User Management → Set Default Player Class** |
| `hasStartTime`, `startTime`, `hasLockTime`, `lockTime`, `hasEndTime`, `endTime` | See [Event times](#event-times) | The database only |
| `enableTranslations` | `1` or `0` | Not used. The language menu is always shown. |

To list them:

```bash
docker exec -it secshep_mariadb mariadb --skip-ssl -uroot -p core -e 'SELECT * FROM settings'
```

### Event times

Security Shepherd can open the levels at a set time, freeze the scoreboard, and close the levels at the end of an event. There's no page for these in the Admin menu, so set them in the database and restart Tomcat.

| Time | While it's set and has passed |
| --- | --- |
| `startTime`, with `hasStartTime = 1` | Before this time, players can't open a level or submit a result key. |
| `lockTime`, with `hasLockTime = 1` | Players can still complete levels, but they no longer get points or medals, so the scoreboard is frozen. |
| `endTime`, with `hasEndTime = 1` | Players can no longer open a level or submit a result key. |

```mermaid
flowchart LR
    before["Before startTime<br/>Levels can't be opened"] --> running["startTime to lockTime<br/>Players complete levels<br/>and score"]
    running --> locked["lockTime to endTime<br/>Completions are recorded,<br/>with no points"]
    locked --> ended["After endTime<br/>Levels can't be opened<br/>or submitted"]
```

The times are in the format `2026-11-14T09:00:00`, in the server's time zone (`TZ` in `.env`). For example, to run an event from 09:00 to 17:00 and freeze the scoreboard for the last hour, open the database:

```bash
docker exec -it secshep_mariadb mariadb --skip-ssl -uroot -p core
```

At the `MariaDB [core]>` prompt, enter:

```sql
UPDATE settings SET value = '2026-11-14T09:00:00' WHERE setting = 'startTime';
UPDATE settings SET value = '2026-11-14T16:00:00' WHERE setting = 'lockTime';
UPDATE settings SET value = '2026-11-14T17:00:00' WHERE setting = 'endTime';
UPDATE settings SET value = '1' WHERE setting IN ('hasStartTime', 'hasLockTime', 'hasEndTime');
```

Then type `exit`, and restart Tomcat:

```bash
docker compose restart web
```

Set `hasStartTime`, `hasLockTime` or `hasEndTime` back to `0` to turn a time off.
