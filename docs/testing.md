# Running Tests

This guide explains how to run the automated test suite for Security Shepherd.

## Prerequisites

- **Docker** and **docker-compose** installed
- **Maven** 3.x
- **JDK 8** or higher
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

## Connection Pool Tests

The connection pool tests (`ConnectionPoolTest`, `DatabaseLifecycleListenerTest`) verify the HikariCP connection pooling implementation. These tests:

- **Require database connectivity** for full coverage
- **Skip gracefully** when database is unavailable
- **Always run** state-management tests that don't need a database

See [database-configuration.md](database-configuration.md) for connection pool configuration details.
