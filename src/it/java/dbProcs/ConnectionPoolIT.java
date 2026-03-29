package dbProcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testUtils.TestProperties;

public class ConnectionPoolIT {

  private static final Logger log = LogManager.getLogger(ConnectionPoolIT.class);
  private static boolean databaseAvailable = false;

  @BeforeAll
  public static void setup() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();

    try {
      ConnectionPool.initialize();
      Connection conn = ConnectionPool.getConnection();
      conn.close();
      databaseAvailable = true;
      log.info("Database is available - running full test suite");
    } catch (Exception e) {
      databaseAvailable = false;
      log.warn("Database not available - skipping connection-dependent tests: " + e.getMessage());
    } finally {
      ConnectionPool.reset();
    }
  }

  @AfterAll
  public static void cleanup() {
    ConnectionPool.shutdown();
  }

  @BeforeEach
  public void resetBeforeEachTest() throws IOException {
    TestProperties.createMysqlResource();
    ConnectionPool.reset();
  }

  private void requireDatabase() {
    assumeTrue(databaseAvailable, "Database not available");
  }

  @Test
  public void testPoolInitializationState() {
    ConnectionPool.reset();
    assertFalse(ConnectionPool.isInitialized(), "Pool should not be initialized before first use");
  }

  @Test
  public void testPoolInitialization() {
    requireDatabase();

    ConnectionPool.reset();
    assertFalse(ConnectionPool.isInitialized(), "Pool should not be initialized before first use");

    ConnectionPool.initialize();
    assertTrue(ConnectionPool.isInitialized(), "Pool should be initialized after initialize()");
  }

  @Test
  public void testConnectionAcquisition() throws SQLException {
    requireDatabase();

    if (!ConnectionPool.isInitialized()) {
      ConnectionPool.initialize();
    }

    Connection conn = null;
    try {
      conn = ConnectionPool.getConnection();
      assertNotNull(conn, "Should be able to get a connection from the pool");
      assertFalse(conn.isClosed(), "Connection should not be closed");
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testConnectionReturn() throws SQLException {
    requireDatabase();

    if (!ConnectionPool.isInitialized()) {
      ConnectionPool.initialize();
    }

    Connection conn = ConnectionPool.getConnection();
    assertNotNull(conn, "Should get a connection");

    conn.close();
    assertTrue(conn.isClosed(), "Connection should appear closed after close()");

    Connection conn2 = ConnectionPool.getConnection();
    assertNotNull(conn2, "Should be able to get another connection");
    conn2.close();
  }

  @Test
  public void testPoolShutdown() throws SQLException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();
    assertTrue(ConnectionPool.isInitialized(), "Pool should be initialized");

    Connection conn = ConnectionPool.getConnection();
    assertNotNull(conn);
    conn.close();

    ConnectionPool.shutdown();
    assertFalse(ConnectionPool.isInitialized(), "Pool should not be initialized after shutdown");
  }

  @Test
  public void testConcurrentConnections() throws InterruptedException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();

    final int numThreads = 10;
    final int operationsPerThread = 5;
    final CountDownLatch startLatch = new CountDownLatch(1);
    final CountDownLatch doneLatch = new CountDownLatch(numThreads);
    final AtomicInteger successCount = new AtomicInteger(0);
    final AtomicInteger errorCount = new AtomicInteger(0);

    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    for (int i = 0; i < numThreads; i++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              for (int j = 0; j < operationsPerThread; j++) {
                Connection conn = null;
                try {
                  conn = ConnectionPool.getConnection();
                  if (conn != null && !conn.isClosed()) {
                    successCount.incrementAndGet();
                  }
                  Thread.sleep(10);
                } catch (SQLException e) {
                  errorCount.incrementAndGet();
                  log.error("Error getting connection: " + e.getMessage());
                } finally {
                  if (conn != null) {
                    try {
                      conn.close();
                    } catch (SQLException e) {
                      log.warn("Error closing connection: " + e.getMessage());
                    }
                  }
                }
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              doneLatch.countDown();
            }
          });
    }

    startLatch.countDown();

    boolean completed = doneLatch.await(60, TimeUnit.SECONDS);
    executor.shutdown();

    assertTrue(completed, "All threads should complete within timeout");
    assertTrue(successCount.get() > 0, "Should have successful connections");
    log.info("Concurrent test: {} successful, {} errors", successCount.get(), errorCount.get());
  }

  @Test
  public void testPoolConfiguration() throws SQLException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();

    String stats = ConnectionPool.getPoolStats();
    assertNotNull(stats, "Pool stats should not be null");
    assertTrue(stats.contains("Pool:"), "Stats should contain pool name");
    log.info("Pool stats: " + stats);
  }

  @Test
  public void testConnectionValidation() throws SQLException {
    requireDatabase();

    if (!ConnectionPool.isInitialized()) {
      ConnectionPool.initialize();
    }

    Connection conn = ConnectionPool.getConnection();
    try {
      assertNotNull(conn, "Connection should not be null");
      assertTrue(conn.isValid(5), "Connection should be valid");
    } finally {
      conn.close();
    }
  }

  @Test
  public void testLazyInitialization() throws SQLException {
    requireDatabase();

    ConnectionPool.reset();
    assertFalse(ConnectionPool.isInitialized(), "Pool should not be initialized");

    Connection conn = ConnectionPool.getConnection();
    assertNotNull(conn, "Should get a connection");
    assertTrue(
        ConnectionPool.isInitialized(), "Pool should be initialized after getting connection");
    conn.close();
  }

  @Test
  public void testChallengeConnectionAcquiresValidConnection() throws SQLException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();

    Connection conn =
        ConnectionPool.getChallengeConnection(
            getTestConnectionUrl(), "core", getTestDbOptions(), "root", getTestPassword());
    try {
      assertNotNull(conn, "Challenge connection should not be null");
      assertFalse(conn.isClosed(), "Challenge connection should not be closed");
      assertTrue(conn.isValid(5), "Challenge connection should be valid");
    } finally {
      conn.close();
    }
  }

  @Test
  public void testChallengePoolReusedForSameCredentials() throws SQLException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();

    assertEquals(0, ConnectionPool.getChallengePoolCount(), "Should start with no challenge pools");

    // First call creates a pool
    Connection conn1 =
        ConnectionPool.getChallengeConnection(
            getTestConnectionUrl(), "core", getTestDbOptions(), "root", getTestPassword());
    conn1.close();
    assertEquals(1, ConnectionPool.getChallengePoolCount(), "Should have one challenge pool");

    // Second call with same credentials should reuse the pool
    Connection conn2 =
        ConnectionPool.getChallengeConnection(
            getTestConnectionUrl(), "core", getTestDbOptions(), "root", getTestPassword());
    conn2.close();
    assertEquals(1, ConnectionPool.getChallengePoolCount(), "Should still have one pool (reused)");
  }

  @Test
  public void testChallengePoolSeparatePerCredentials() throws SQLException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();

    // Create a pool with one set of credentials
    Connection conn1 =
        ConnectionPool.getChallengeConnection(
            getTestConnectionUrl(), "core", getTestDbOptions(), "root", getTestPassword());
    conn1.close();
    assertEquals(1, ConnectionPool.getChallengePoolCount(), "Should have one challenge pool");

    // Create a pool with different schema (simulates different challenge)
    Connection conn2 =
        ConnectionPool.getChallengeConnection(
            getTestConnectionUrl(),
            "information_schema",
            getTestDbOptions(),
            "root",
            getTestPassword());
    conn2.close();
    assertEquals(
        2, ConnectionPool.getChallengePoolCount(), "Should have two pools for different schemas");
  }

  @Test
  public void testChallengePoolHasCorrectSizing() throws SQLException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();

    String url = getTestConnectionUrl();
    String schema = "core";
    String options = getTestDbOptions();
    String username = "root";
    String password = getTestPassword();

    Connection conn =
        ConnectionPool.getChallengeConnection(url, schema, options, username, password);
    conn.close();

    // Build the pool key the same way ConnectionPool does
    String jdbcUrl = url + schema + "?" + options;
    String poolKey = jdbcUrl + ":" + username;

    HikariDataSource ds = ConnectionPool.getChallengePool(poolKey);
    assertNotNull(ds, "Challenge pool should exist for key");
    assertEquals(3, ds.getMaximumPoolSize(), "Challenge pool maxPoolSize should be 3");
    assertEquals(0, ds.getMinimumIdle(), "Challenge pool minIdle should be 0");
    assertEquals(120000, ds.getIdleTimeout(), "Challenge pool idleTimeout should be 2 minutes");
  }

  @Test
  public void testChallengePoolMaxConnectionsEnforced() throws SQLException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();

    // Acquire all 3 max connections
    List<Connection> connections = new ArrayList<>();
    try {
      for (int i = 0; i < 3; i++) {
        Connection conn =
            ConnectionPool.getChallengeConnection(
                getTestConnectionUrl(), "core", getTestDbOptions(), "root", getTestPassword());
        assertNotNull(conn, "Should get connection " + (i + 1));
        connections.add(conn);
      }

      // The 4th connection should block and eventually timeout since pool max is 3
      // We can't easily test the timeout without waiting 30s, so instead verify
      // the pool reports the right active count
      String url = getTestConnectionUrl();
      String jdbcUrl = url + "core" + "?" + getTestDbOptions();
      String poolKey = jdbcUrl + ":" + "root";
      HikariDataSource ds = ConnectionPool.getChallengePool(poolKey);
      assertEquals(
          3, ds.getHikariPoolMXBean().getActiveConnections(), "Should have 3 active connections");
    } finally {
      for (Connection conn : connections) {
        conn.close();
      }
    }
  }

  @Test
  public void testShutdownClosesAllChallengePools() throws SQLException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();

    // Create two challenge pools
    Connection conn1 =
        ConnectionPool.getChallengeConnection(
            getTestConnectionUrl(), "core", getTestDbOptions(), "root", getTestPassword());
    conn1.close();
    Connection conn2 =
        ConnectionPool.getChallengeConnection(
            getTestConnectionUrl(),
            "information_schema",
            getTestDbOptions(),
            "root",
            getTestPassword());
    conn2.close();
    assertEquals(2, ConnectionPool.getChallengePoolCount(), "Should have two challenge pools");

    ConnectionPool.shutdown();
    assertEquals(
        0, ConnectionPool.getChallengePoolCount(), "Shutdown should clear all challenge pools");
  }

  @Test
  public void testConcurrentChallengeConnections() throws InterruptedException {
    requireDatabase();

    ConnectionPool.reset();
    ConnectionPool.initialize();

    final int numThreads = 6;
    final int operationsPerThread = 5;
    final CountDownLatch startLatch = new CountDownLatch(1);
    final CountDownLatch doneLatch = new CountDownLatch(numThreads);
    final AtomicInteger successCount = new AtomicInteger(0);
    final AtomicInteger errorCount = new AtomicInteger(0);

    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    for (int i = 0; i < numThreads; i++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              for (int j = 0; j < operationsPerThread; j++) {
                Connection conn = null;
                try {
                  conn =
                      ConnectionPool.getChallengeConnection(
                          getTestConnectionUrl(),
                          "core",
                          getTestDbOptions(),
                          "root",
                          getTestPassword());
                  if (conn != null && !conn.isClosed()) {
                    successCount.incrementAndGet();
                  }
                  Thread.sleep(10);
                } catch (SQLException e) {
                  errorCount.incrementAndGet();
                  log.error("Error getting challenge connection: " + e.getMessage());
                } finally {
                  if (conn != null) {
                    try {
                      conn.close();
                    } catch (SQLException e) {
                      log.warn("Error closing challenge connection: " + e.getMessage());
                    }
                  }
                }
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              doneLatch.countDown();
            }
          });
    }

    startLatch.countDown();

    boolean completed = doneLatch.await(60, TimeUnit.SECONDS);
    executor.shutdown();

    assertTrue(completed, "All threads should complete within timeout");
    assertEquals(
        numThreads * operationsPerThread,
        successCount.get(),
        "All challenge connection attempts should succeed");
    assertEquals(0, errorCount.get(), "Should have no errors");
    assertEquals(
        1,
        ConnectionPool.getChallengePoolCount(),
        "Should have exactly one challenge pool (all threads used same credentials)");
  }

  @Test
  public void testPoolShutdownWithoutInit() {
    ConnectionPool.reset();
    assertFalse(ConnectionPool.isInitialized(), "Pool should not be initialized");

    ConnectionPool.shutdown();

    assertFalse(
        ConnectionPool.isInitialized(), "Pool should still not be initialized after shutdown");
  }

  @Test
  public void testResetIsIdempotent() {
    ConnectionPool.reset();
    ConnectionPool.reset();
    ConnectionPool.reset();

    assertFalse(
        ConnectionPool.isInitialized(), "Pool should not be initialized after multiple resets");
  }

  private static String getTestConnectionUrl() {
    io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
    String host = dotenv.get("TEST_MYSQL_HOST");
    String port = dotenv.get("TEST_MYSQL_PORT");
    return "jdbc:mariadb://" + host + ":" + port + "/";
  }

  private static String getTestPassword() {
    io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
    return dotenv.get("TEST_MYSQL_PASSWORD");
  }

  private static String getTestDbOptions() {
    return "useUnicode=true&character_set_server=utf8mb4";
  }
}
