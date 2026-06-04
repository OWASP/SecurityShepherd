package dbProcs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Connection pool manager using HikariCP for efficient database connection management.
 *
 * <p>This class provides connection pooling for MySQL/MariaDB databases, significantly improving
 * performance by reusing connections instead of creating new ones for each request.
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * @author Paul
 */
public class ConnectionPool {

  private static final Logger log = LogManager.getLogger(ConnectionPool.class);

  // Singleton instance for the core database pool
  private static volatile HikariDataSource coreDataSource;

  // Lock object for thread-safe initialization
  private static final Object lock = new Object();

  // Challenge-specific connection pools (keyed by schema name)
  private static final ConcurrentHashMap<String, HikariDataSource> challengePools =
      new ConcurrentHashMap<>();

  // Default pool configuration values (used by core pool)
  private static final int DEFAULT_MAX_POOL_SIZE = 20;
  private static final int DEFAULT_MIN_IDLE = 5;
  private static final long DEFAULT_CONNECTION_TIMEOUT =
      5000; // 5 seconds — fail fast under overload
  private static final long DEFAULT_IDLE_TIMEOUT = 600000; // 10 minutes
  private static final long DEFAULT_MAX_LIFETIME = 1800000; // 30 minutes
  private static final long DEFAULT_LEAK_DETECTION_THRESHOLD = 60000; // 60 seconds

  // Challenge pool configuration values (smaller footprint per schema)
  private static final int CHALLENGE_MAX_POOL_SIZE = 3;
  private static final int CHALLENGE_MIN_IDLE = 0;
  private static final long CHALLENGE_IDLE_TIMEOUT = 120000; // 2 minutes

  // Flag to track if pool has been initialized
  private static volatile boolean initialized = false;

  /** Private constructor to prevent instantiation */
  private ConnectionPool() {}

  /**
   * Initialize the connection pool. This method is idempotent - calling it multiple times has no
   * effect after the first successful initialization.
   */
  public static void initialize() {
    if (!initialized) {
      synchronized (lock) {
        if (!initialized) {
          try {
            coreDataSource = createCoreDataSource();
            initialized = true;
            log.info("Connection pool initialized successfully");
          } catch (Exception e) {
            log.error("Failed to initialize connection pool: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize connection pool", e);
          }
        }
      }
    }
  }

  /**
   * Creates the HikariDataSource for the core database.
   *
   * @return Configured HikariDataSource
   */
  private static HikariDataSource createCoreDataSource() {
    Properties prop = loadDatabaseProperties();

    String connectionURL = prop.getProperty("databaseConnectionURL");
    String databaseSchema = prop.getProperty("databaseSchema");
    String dbOptions = prop.getProperty("databaseOptions");
    String username = prop.getProperty("databaseUsername");
    String password = prop.getProperty("databasePassword");

    // Build the full JDBC URL
    String jdbcUrl = connectionURL + databaseSchema;
    if (dbOptions != null && !dbOptions.isEmpty()) {
      jdbcUrl += "?" + dbOptions;
    }

    return createDataSource(jdbcUrl, username, password, prop, "CorePool");
  }

  /**
   * Creates a HikariDataSource with the given configuration.
   *
   * @param jdbcUrl The JDBC URL
   * @param username Database username
   * @param password Database password
   * @param prop Properties containing pool configuration
   * @param poolName Name for the pool (for logging/monitoring)
   * @return Configured HikariDataSource
   */
  private static HikariDataSource createDataSource(
      String jdbcUrl, String username, String password, Properties prop, String poolName) {

    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    config.setPoolName(poolName);

    String driverClassName = prop.getProperty("DriverType");
    if (driverClassName == null || driverClassName.isEmpty()) {
      if (jdbcUrl.startsWith("jdbc:mariadb:")) {
        driverClassName = "org.mariadb.jdbc.Driver";
      } else if (jdbcUrl.startsWith("jdbc:mysql:")) {
        driverClassName = "com.mysql.cj.jdbc.Driver";
      } else {
        throw new IllegalArgumentException("Unsupported JDBC URL: " + jdbcUrl);
      }
    }
    config.setDriverClassName(driverClassName);

    // Pool size configuration
    config.setMaximumPoolSize(getIntProperty(prop, "pool.maximumPoolSize", DEFAULT_MAX_POOL_SIZE));
    config.setMinimumIdle(getIntProperty(prop, "pool.minimumIdle", DEFAULT_MIN_IDLE));

    // Timeout configuration
    config.setConnectionTimeout(
        getLongProperty(prop, "pool.connectionTimeout", DEFAULT_CONNECTION_TIMEOUT));
    config.setIdleTimeout(getLongProperty(prop, "pool.idleTimeout", DEFAULT_IDLE_TIMEOUT));
    config.setMaxLifetime(getLongProperty(prop, "pool.maxLifetime", DEFAULT_MAX_LIFETIME));

    // Leak detection - logs a warning if a connection is held longer than this threshold
    config.setLeakDetectionThreshold(
        getLongProperty(prop, "pool.leakDetectionThreshold", DEFAULT_LEAK_DETECTION_THRESHOLD));

    // Connection validation
    config.setConnectionTestQuery("SELECT 1");

    // Performance optimizations
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");

    log.debug(
        "Creating HikariCP pool '{}' with maxPoolSize={}, minIdle={}",
        poolName,
        config.getMaximumPoolSize(),
        config.getMinimumIdle());

    return new HikariDataSource(config);
  }

  /**
   * Creates a HikariDataSource with custom pool size settings. Used for challenge pools which need
   * a smaller resource footprint than the core pool.
   *
   * @param jdbcUrl The JDBC URL
   * @param username Database username
   * @param password Database password
   * @param prop Properties containing pool configuration
   * @param poolName Name for the pool (for logging/monitoring)
   * @param maxPoolSize Maximum number of connections in the pool
   * @param minIdle Minimum number of idle connections maintained
   * @param idleTimeout Idle timeout in milliseconds before a connection is retired
   * @return Configured HikariDataSource
   */
  private static HikariDataSource createDataSource(
      String jdbcUrl,
      String username,
      String password,
      Properties prop,
      String poolName,
      int maxPoolSize,
      int minIdle,
      long idleTimeout) {

    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    config.setPoolName(poolName);

    String driverClassName = prop.getProperty("DriverType");
    if (driverClassName == null || driverClassName.isEmpty()) {
      if (jdbcUrl.startsWith("jdbc:mariadb:")) {
        driverClassName = "org.mariadb.jdbc.Driver";
      } else if (jdbcUrl.startsWith("jdbc:mysql:")) {
        driverClassName = "com.mysql.cj.jdbc.Driver";
      } else {
        throw new IllegalArgumentException("Unsupported JDBC URL: " + jdbcUrl);
      }
    }
    config.setDriverClassName(driverClassName);

    // Pool size configuration (using provided values instead of defaults)
    config.setMaximumPoolSize(maxPoolSize);
    config.setMinimumIdle(minIdle);

    // Timeout configuration
    config.setConnectionTimeout(
        getLongProperty(prop, "pool.connectionTimeout", DEFAULT_CONNECTION_TIMEOUT));
    config.setIdleTimeout(idleTimeout);
    config.setMaxLifetime(getLongProperty(prop, "pool.maxLifetime", DEFAULT_MAX_LIFETIME));

    // Leak detection - logs a warning if a connection is held longer than this threshold
    config.setLeakDetectionThreshold(
        getLongProperty(prop, "pool.leakDetectionThreshold", DEFAULT_LEAK_DETECTION_THRESHOLD));

    // Connection validation
    config.setConnectionTestQuery("SELECT 1");

    // Performance optimizations
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");

    log.debug(
        "Creating HikariCP pool '{}' with maxPoolSize={}, minIdle={}, idleTimeout={}",
        poolName,
        config.getMaximumPoolSize(),
        config.getMinimumIdle(),
        config.getIdleTimeout());

    return new HikariDataSource(config);
  }

  /**
   * Loads database properties from the configuration file.
   *
   * @return Properties object containing database configuration
   */
  private static Properties loadDatabaseProperties() {
    Properties prop = new Properties();
    String mysqlProps = Constants.MYSQL_DB_PROP;

    try (InputStream input = new FileInputStream(mysqlProps)) {
      prop.load(input);
    } catch (IOException e) {
      log.error("Could not load database properties file: " + e.getMessage(), e);
      throw new RuntimeException("Could not load database properties", e);
    }

    return prop;
  }

  /**
   * Loads database properties if the file exists; otherwise returns an empty Properties object.
   * Used by the challenge-connection path during first-time setup, when callers supply
   * url/user/password as arguments and the on-disk file does not yet exist. All pool tuning
   * properties have defaults, so an empty Properties object is safe.
   */
  private static Properties loadDatabasePropertiesIfPresent() {
    if (!new File(Constants.MYSQL_DB_PROP).isFile()) {
      return new Properties();
    }
    return loadDatabaseProperties();
  }

  /**
   * Gets a connection from the core database pool.
   *
   * @return A connection from the pool
   * @throws SQLException if a connection cannot be obtained
   */
  public static Connection getConnection() throws SQLException {
    if (!initialized) {
      try {
        initialize();
      } catch (RuntimeException e) {
        throw new SQLException("Connection pool not available", e);
      }
    }
    return coreDataSource.getConnection();
  }

  /**
   * Gets a connection for a specific challenge schema.
   *
   * @param connectionURL The base connection URL
   * @param challengeConnectionURL The challenge-specific part of the URL
   * @param dbOptions Database options
   * @param username Database username
   * @param password Database password
   * @return A connection from the challenge-specific pool
   * @throws SQLException if a connection cannot be obtained
   */
  public static Connection getChallengeConnection(
      String connectionURL,
      String challengeConnectionURL,
      String dbOptions,
      String username,
      String password)
      throws SQLException {

    // Build the full JDBC URL
    final String jdbcUrl;
    if (dbOptions != null && !dbOptions.isEmpty()) {
      jdbcUrl = connectionURL + challengeConnectionURL + "?" + dbOptions;
    } else {
      jdbcUrl = connectionURL + challengeConnectionURL;
    }

    // Use the full URL and username as the pool key
    String poolKey = jdbcUrl + ":" + username;

    HikariDataSource dataSource =
        challengePools.computeIfAbsent(
            poolKey,
            key -> {
              // Tolerate missing database.properties: during first-time setup the file does
              // not exist yet, but the caller already supplied url/user/password. Pool tuning
              // falls back to defaults when properties are absent.
              Properties prop = loadDatabasePropertiesIfPresent();
              return createDataSource(
                  jdbcUrl,
                  username,
                  password,
                  prop,
                  "ChallengePool-" + username,
                  CHALLENGE_MAX_POOL_SIZE,
                  CHALLENGE_MIN_IDLE,
                  CHALLENGE_IDLE_TIMEOUT);
            });

    return dataSource.getConnection();
  }

  /**
   * Shuts down all connection pools. This should be called when the application is shutting down.
   */
  public static void shutdown() {
    synchronized (lock) {
      log.info("Shutting down connection pools...");

      // Close core pool
      if (coreDataSource != null && !coreDataSource.isClosed()) {
        coreDataSource.close();
        log.debug("Core connection pool closed");
      }

      // Close all challenge pools
      for (HikariDataSource ds : challengePools.values()) {
        if (!ds.isClosed()) {
          ds.close();
        }
      }
      challengePools.clear();

      coreDataSource = null;
      initialized = false;

      log.info("All connection pools shut down successfully");
    }
  }

  /**
   * Checks if the connection pool has been initialized.
   *
   * @return true if initialized, false otherwise
   */
  public static boolean isInitialized() {
    return initialized;
  }

  /**
   * Gets pool statistics for monitoring (useful for debugging).
   *
   * @return String containing pool statistics
   */
  public static String getPoolStats() {
    if (coreDataSource == null) {
      return "Pool not initialized";
    }

    return String.format(
        "Pool: %s, Active: %d, Idle: %d, Total: %d, Waiting: %d",
        coreDataSource.getPoolName(),
        coreDataSource.getHikariPoolMXBean().getActiveConnections(),
        coreDataSource.getHikariPoolMXBean().getIdleConnections(),
        coreDataSource.getHikariPoolMXBean().getTotalConnections(),
        coreDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
  }

  /**
   * Returns the number of connections currently checked out from the core pool. Intended for
   * integration tests that verify connections are returned after Getter/Setter operations.
   *
   * @return active connection count, or -1 if the pool is not initialized
   */
  public static int getCoreActiveConnections() {
    if (coreDataSource == null || coreDataSource.isClosed()) {
      return -1;
    }
    return coreDataSource.getHikariPoolMXBean().getActiveConnections();
  }

  /** Helper method to get an integer property with a default value. */
  private static int getIntProperty(Properties prop, String key, int defaultValue) {
    String value = prop.getProperty(key);
    if (value != null) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        log.warn(
            "Invalid integer value for property '{}': {}, using default: {}",
            key,
            value,
            defaultValue);
      }
    }
    return defaultValue;
  }

  /** Helper method to get a long property with a default value. */
  private static long getLongProperty(Properties prop, String key, long defaultValue) {
    String value = prop.getProperty(key);
    if (value != null) {
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
        log.warn(
            "Invalid long value for property '{}': {}, using default: {}",
            key,
            value,
            defaultValue);
      }
    }
    return defaultValue;
  }

  /** Resets the pool state. This is primarily for testing purposes. */
  public static void reset() {
    shutdown();
  }

  /**
   * Returns the number of challenge pools currently active. Intended for testing and monitoring.
   *
   * @return the number of challenge pools
   */
  public static int getChallengePoolCount() {
    return challengePools.size();
  }

  /**
   * Returns the HikariDataSource for a challenge pool by key, or null if not found. Intended for
   * testing to verify pool configuration.
   *
   * @param poolKey the pool key (jdbcUrl:username)
   * @return the HikariDataSource, or null
   */
  static HikariDataSource getChallengePool(String poolKey) {
    return challengePools.get(poolKey);
  }
}
