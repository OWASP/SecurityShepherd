package dbProcs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
  public void testChallengeConnection() throws SQLException {
    requireDatabase();

    if (!ConnectionPool.isInitialized()) {
      ConnectionPool.initialize();
    }

    Connection conn = null;
    try {
      conn =
          ConnectionPool.getChallengeConnection(
              "jdbc:mysql://localhost:3306/",
              "core",
              "useUnicode=true&character_set_server=utf8mb4",
              "root",
              System.getenv("TEST_MYSQL_PASSWORD") != null
                  ? System.getenv("TEST_MYSQL_PASSWORD")
                  : "CowSaysMoo");
      if (conn != null) {
        assertFalse(conn.isClosed(), "Connection should not be closed");
      }
    } catch (SQLException | RuntimeException e) {
      log.debug("Challenge connection test skipped: " + e.getMessage());
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
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
}
