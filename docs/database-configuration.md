# Database Configuration

This guide covers the database connection configuration for Security Shepherd, including the connection pooling implementation.

## Overview

Security Shepherd uses two databases:

- **MariaDB/MySQL** - Main application database (user accounts, progress, modules)
- **MongoDB** - Challenge-specific data storage

## Why Connection Pooling?

### The Problem Without Pooling

Without connection pooling, every database operation requires:
1. Opening a new TCP connection
2. Performing authentication handshake
3. Executing the query
4. Closing the connection

This is expensive - each connection can take 20-50ms to establish, which adds up quickly under load.

### Pros of Connection Pooling

| Benefit | Description |
|---------|-------------|
| **Performance** | Connections are reused, eliminating connection overhead per request |
| **Resource Efficiency** | Limits total connections, preventing database overload |
| **Connection Validation** | Pool validates connections before use, catching stale connections |
| **Configurable** | Tune pool size, timeouts, and behavior for your workload |
| **Monitoring** | Pool provides statistics on usage, active connections, and wait times |

### Cons of Connection Pooling

| Drawback | Description |
|----------|-------------|
| **Memory Overhead** | Idle connections consume memory on both app and database servers |
| **Configuration Complexity** | Wrong pool size can cause issues (too small = contention, too large = resource waste) |
| **Connection Leaks** | Code that doesn't close connections properly can exhaust the pool |
| **Stale Connections** | Long-idle connections may be terminated by firewalls or the database |
| **Debugging Complexity** | Connection issues can be harder to diagnose with pooling |

### When Pooling Makes Sense

Connection pooling is beneficial when:
- Your application handles many concurrent requests
- Database operations are frequent
- Connection establishment time is significant relative to query time

For Security Shepherd, pooling improves performance during:
- Multiple users accessing challenges simultaneously
- Scoreboard updates and leaderboard queries
- User authentication and session management

## Connection Pooling Implementation

### MySQL/MariaDB - HikariCP

Security Shepherd uses [HikariCP](https://github.com/brettwooldridge/HikariCP) for MySQL/MariaDB connection pooling. HikariCP is a high-performance JDBC connection pool.

**Benefits:**
- Efficient connection reuse
- Reduced connection overhead
- Configurable pool size
- Connection validation and health checks

### MongoDB - Singleton Pattern

MongoDB connections use a singleton `MongoClient` instance. The MongoDB Java driver includes built-in connection pooling, so we maintain a single client instance that's reused across the application.

## Configuration Files

### MySQL/MariaDB Configuration

File: `src/main/resources/database.properties`

See example: `src/main/resources/database.properties.example`

```properties
# Database connection URL (without schema)
databaseConnectionURL=jdbc:mysql://localhost:3306/

# JDBC driver class
DriverType=org.gjt.mm.mysql.Driver

# Connection options
databaseOptions=useUnicode=true&character_set_server=utf8mb4

# Database schema name
databaseSchema=core

# Credentials
databaseUsername=root
databasePassword=your_password

# HikariCP Pool Settings (optional - defaults shown)
pool.maximumPoolSize=20
pool.minimumIdle=5
pool.connectionTimeout=5000
pool.idleTimeout=600000
pool.maxLifetime=1800000
pool.poolName=SecurityShepherdPool
```

### Pool Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| `pool.maximumPoolSize` | 20 | Maximum number of connections in the pool |
| `pool.minimumIdle` | 5 | Minimum number of idle connections to maintain |
| `pool.connectionTimeout` | 5000 | Maximum time (ms) to wait for a connection |
| `pool.idleTimeout` | 600000 | Maximum time (ms) a connection can be idle |
| `pool.maxLifetime` | 1800000 | Maximum lifetime (ms) of a connection |
| `pool.leakDetectionThreshold` | 60000 | Logs a warning if a connection is held longer than this (ms). Set to 0 to disable |
| `pool.poolName` | SecurityShepherdPool | Name for the pool (appears in logs) |

### MongoDB Configuration

File: `src/main/resources/mongo.properties`

See example: `src/main/resources/mongo.properties.example`

```properties
# MongoDB connection settings
connectionHost=localhost
connectionPort=27017
databaseName=shepherdGames
connectTimeout=1000
socketTimeout=0
serverSelectionTimeout=30000

# Connection pool settings (optional)
connectionsPerHost=10
minConnectionsPerHost=2
```

## Lifecycle Management

Connection pools are managed by `DatabaseLifecycleListener`, which:

1. **On application startup**: Initializes the HikariCP connection pool
2. **On application shutdown**: Closes all database connections gracefully

This is registered in `web.xml`:

```xml
<listener>
    <listener-class>listeners.DatabaseLifecycleListener</listener-class>
</listener>
```

## Monitoring

### Pool Statistics

The `ConnectionPool` class provides pool statistics:

```java
String stats = ConnectionPool.getPoolStats();
// Returns: Pool: SecurityShepherdPool | Total: 10 | Active: 2 | Idle: 8 | Waiting: 0
```

### Logging

Connection pool events are logged at various levels:

- **INFO**: Pool initialization and shutdown
- **DEBUG**: Connection acquisition and pool statistics
- **WARN**: Connection validation failures
- **ERROR**: Pool initialization failures

## Docker Configuration

When running with Docker, database hosts are configured via environment variables in `.env`:

```properties
# MariaDB
CONTAINER_MARIADB=secshep_mariadb
DB_PASS=your_password
DB_PORT=3306

# MongoDB  
CONTAINER_MONGO=secshep_mongo
```

The Tomcat container connects to databases using container names (e.g., `secshep_mariadb`) as hostnames within the Docker network.

### First-Time Setup

After starting the stack with `docker compose up`, the app will redirect to `https://localhost/setup.jsp` for initial database configuration.

> **Note:** The TLS certificate is self-signed. Your browser will show a security warning — accept it to proceed.

#### Step 1: Get the authentication token

The setup page requires a token from the server's filesystem to prevent unauthorized configuration:

```bash
docker exec secshep_tomcat cat /usr/local/tomcat/conf/SecurityShepherd.auth
```

#### Step 2: Fill in the setup form

Navigate to `https://localhost/setup.jsp` and fill in:

| Field | Value | Notes |
|-------|-------|-------|
| **Hostname** | `secshep_mariadb` | The Docker container name — **not** `localhost` |
| **Port** | `3306` | |
| **DB Username** | `root` | |
| **DB Password** | `CowSaysMoo` | Must match `DB_PASS` in `.env` |
| **Override Databases** | checked | Initializes all challenge schemas on first setup |
| **MongoDB Host** | `secshep_mongo` | The Docker container name — **not** `localhost` |
| **MongoDB Port** | `27017` | |
| **Authentication token** | (paste from step 1) | |

#### Step 3: Submit

Click submit. On success you will see "Database Configuration Complete" and be redirected to the login page.

#### Alternative: setup via curl

```bash
AUTH=$(docker exec secshep_tomcat cat /usr/local/tomcat/conf/SecurityShepherd.auth)
curl -k -s -X POST https://localhost/setup \
  -d "dbhost=secshep_mariadb" \
  -d "dbport=3306" \
  -d "dbuser=root" \
  -d "dbpass=CowSaysMoo" \
  -d "dboverride=override" \
  -d "dbauth=$AUTH" \
  -d "mhost=secshep_mongo" \
  -d "mport=27017"
```

> **Important:** The hostnames must be Docker container names (e.g. `secshep_mariadb`, `secshep_mongo`), not `localhost`. The Tomcat container connects to the databases over the Docker network, where containers are addressed by name.

## Troubleshooting

### Pool Exhaustion

If you see "Connection is not available" errors:

1. Check for connection leaks (connections not being closed)
2. Increase `pool.maximumPoolSize`
3. Review `pool.connectionTimeout` setting

### Slow Connection Acquisition

If connections are slow to acquire:

1. Check database server health
2. Review network latency between app and database
3. Consider increasing `pool.minimumIdle`

### Connection Validation Failures

If connections are failing validation:

1. Check database server is running
2. Verify credentials in properties file
3. Check network connectivity

### MariaDB Container Exits with SQL Syntax Error

If the MariaDB container exits immediately on first startup with an error like:

```
ERROR 1064 (42000) at line 181: You have an error in your SQL syntax;
check the manual that corresponds to your MariaDB server version for the right syntax to use near '' at line 3
```

This is a **stale Docker image cache** issue. The SQL source files in `src/main/resources/database/` have `DELIMITER` statements commented out for compatibility with tools that don't support `DELIMITER`. During the Maven build (`mvn -Pdocker`), a script (`docker/scripts/convert-sql-scripts.sh`) uncomments them in the copies under `docker/mariadb/target/`. If Docker reuses a cached image from before this conversion ran, the stored procedures fail to parse.

**Fix:**

```bash
mvn -Pdocker validate                    # ensure SQL scripts are converted
docker compose build --no-cache db       # rebuild without cache
docker compose down -v                   # remove old volumes with failed init
docker compose up -d db                  # start fresh
```

Note: `docker compose down -v` is needed because MariaDB only runs init scripts on first startup with an empty data volume. If the previous attempt partially initialized, the scripts won't re-run without removing the volume.
