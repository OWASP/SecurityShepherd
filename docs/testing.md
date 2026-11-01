# Running Tests

This guide explains how to run the automated test suite for Security Shepherd.

## Prerequisites

- **Docker** and **docker-compose** installed
- **Maven 3.9.2 or newer** 
- **JDK 17** (the build targets Java 17)
- `.env` file configured (copy from project or create one)

## Environment Variables

Tests use the `dotenv` library to load database credentials from the `.env` file. Required variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `TEST_MYSQL_HOST` | MySQL/MariaDB host | `127.0.0.1` |
| `TEST_MYSQL_PORT` | MySQL/MariaDB port | `3306` |
| `TEST_MYSQL_PASSWORD` | MySQL root password | Must match `DB_PASS` |
| `TEST_MONGO_HOST` | MongoDB host | `127.0.0.1` |
| `TEST_MONGO_PORT` | MongoDB port | `27017` |

**Important**: `TEST_MYSQL_PASSWORD` must match the `DB_PASS` value used when the database container was created.

## Running Tests with Docker

### Step 1: Start Database Containers

Start only the database containers (not the web application):

```bash
docker-compose up -d db mongo
```

Wait 15-30 seconds for the databases to initialize fully.

### Step 2: Verify Containers Are Running

```bash
docker-compose ps
```

You should see `secshep_mariadb` and `secshep_mongo` with status "Up".

### Step 3: Run the Tests

```bash
mvn test
```

### Step 4: Stop Containers

When finished:

```bash
docker-compose down
```

## Running Specific Tests

Run a single test class:

```bash
mvn test -Dtest=GetterTest
```

Run multiple test classes:

```bash
mvn test -Dtest=GetterTest,SetterTest
```

Run tests matching a pattern:

```bash
mvn test -Dtest=*Pool*
```

## Understanding Skipped Tests

Some tests will show as "skipped" rather than failed. This happens when:

1. **Database is not running** - Tests that require database connectivity are skipped
2. **Credentials mismatch** - `TEST_MYSQL_PASSWORD` doesn't match `DB_PASS`
3. **Connection refused** - Database container isn't ready yet

This is intentional behavior. It allows basic unit tests to run even without a full database setup, while integration tests are skipped gracefully.

### Example Output

```
Tests run: 16, Failures: 0, Errors: 0, Skipped: 11
```

This indicates 5 tests ran successfully and 11 were skipped (likely database-dependent tests).

## Troubleshooting

### "Access denied for user 'root'"

The password in your `.env` file doesn't match what the database was created with. Options:

1. Update `TEST_MYSQL_PASSWORD` to match `DB_PASS`
2. Or recreate the database volume:

```bash
docker-compose down -v
docker-compose up -d db mongo
```

### "Connection refused"

The database container isn't running or isn't ready:

```bash
# Check container status
docker-compose ps

# Check database logs
docker-compose logs db

# Wait and retry
sleep 30
mvn test
```

### Tests Still Failing After Fix

Make sure Maven picks up the latest code:

```bash
mvn clean test
```

## Smoke Testing the Docker Images

The unit and integration tests run on your own JDK, not in the Docker images. `src/test/scripts/image-smoke-test.sh` runs the app from the images instead. It starts the Tomcat, MariaDB and Mongo images, runs the first-time setup, logs in as `admin`, changes the temporary password, which uses the native Argon2 library in the image, and loads and solves the SQL injection lesson, which uses its own database user. It fails if any of that fails or if Tomcat logs a `SEVERE` error. CI runs it after `docker compose build`.

Run it from the project root after building the images:

```bash
mvn clean install -Pdocker -DskipTests -B
docker compose build
bash src/test/scripts/image-smoke-test.sh
```

The script never builds or pulls an image. It tests the images named by `IMAGE_TOMCAT`, `IMAGE_MARIADB` and `IMAGE_MONGO`, from the environment or from `.env`. It runs as a compose project of its own, with its own containers and volumes, which it removes when it finishes. It uses host ports 18080, 18443, 13307 and 27018, so it can run next to your own `secshep_*` stack. If those ports are taken, set `SMOKE_HTTP_PORT`, `SMOKE_HTTPS_PORT`, `SMOKE_DB_PORT` or `SMOKE_MONGO_PORT`.

## Connection Pool Tests

The connection pool tests (`ConnectionPoolTest`, `DatabaseLifecycleListenerTest`) verify the HikariCP connection pooling implementation. These tests:

- **Require database connectivity** for full coverage
- **Skip gracefully** when database is unavailable
- **Always run** state-management tests that don't need a database

See [Database layer](database-layer.md) for how the connection pools work, and [Configuration](configuration.md#databaseproperties) for their settings.
