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
pool.maximumPoolSize=10
pool.minimumIdle=2
pool.connectionTimeout=30000
pool.idleTimeout=600000
pool.maxLifetime=1800000
pool.poolName=SecurityShepherdPool
```

### Pool Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| `pool.maximumPoolSize` | 10 | Maximum number of connections in the pool |
| `pool.minimumIdle` | 2 | Minimum number of idle connections to maintain |
| `pool.connectionTimeout` | 30000 | Maximum time (ms) to wait for a connection |
| `pool.idleTimeout` | 600000 | Maximum time (ms) a connection can be idle |
| `pool.maxLifetime` | 1800000 | Maximum lifetime (ms) of a connection |
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
