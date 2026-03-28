package dbProcs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

  // Default pool configuration values
  private static final int DEFAULT_MAX_POOL_SIZE = 10;
  private static final int DEFAULT_MIN_IDLE = 2;
  private static final long DEFAULT_CONNECTION_TIMEOUT = 30000; // 30 seconds
  private static final long DEFAULT_IDLE_TIMEOUT = 600000; // 10 minutes
  private static final long DEFAULT_MAX_LIFETIME = 1800000; // 30 minutes

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

    // Pool size configuration
    config.setMaximumPoolSize(getIntProperty(prop, "pool.maximumPoolSize", DEFAULT_MAX_POOL_SIZE));
    config.setMinimumIdle(getIntProperty(prop, "pool.minimumIdle", DEFAULT_MIN_IDLE));

    // Timeout configuration
    config.setConnectionTimeout(
        getLongProperty(prop, "pool.connectionTimeout", DEFAULT_CONNECTION_TIMEOUT));
    config.setIdleTimeout(getLongProperty(prop, "pool.idleTimeout", DEFAULT_IDLE_TIMEOUT));
    config.setMaxLifetime(getLongProperty(prop, "pool.maxLifetime", DEFAULT_MAX_LIFETIME));

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
              Properties prop = loadDatabaseProperties();
              return createDataSource(
                  jdbcUrl, username, password, prop, "ChallengePool-" + username);
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
}
