package listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dbProcs.ConnectionPool;
import dbProcs.MongoDatabase;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletContextEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import testUtils.TestProperties;

/**
 * Unit tests for the DatabaseLifecycleListener class.
 *
 * <p>Note: Tests that require actual database connectivity will be skipped if the database is not
 * available. Run with a proper database setup for full coverage.
 */
public class DatabaseLifecycleListenerTest {

  private static final Logger log = LogManager.getLogger(DatabaseLifecycleListenerTest.class);
  private static boolean databaseAvailable = false;

  @Mock private ServletContextEvent mockServletContextEvent;

  private DatabaseLifecycleListener listener;

  @BeforeAll
  public static void setupClass() throws IOException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.createMongoResource();

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

  @BeforeEach
  public void setup() throws IOException {
    MockitoAnnotations.openMocks(this);
    ConnectionPool.reset();
    MongoDatabase.resetInstance();
    TestProperties.createMysqlResource();
    TestProperties.createMongoResource();
    listener = new DatabaseLifecycleListener();
  }

  private void requireDatabase() {
    assumeTrue(databaseAvailable, "Database not available");
  }

  @Test
  @DisplayName("contextInitialized should initialize the connection pool")
  public void testContextInitialized() {
    requireDatabase();

    assertFalse(
        ConnectionPool.isInitialized(), "Pool should not be initialized before contextInitialized");
    listener.contextInitialized(mockServletContextEvent);
    assertTrue(
        ConnectionPool.isInitialized(), "Pool should be initialized after contextInitialized");
  }

  @Test
  @DisplayName("contextDestroyed should shut down all pools")
  public void testContextDestroyed() {
    requireDatabase();

    listener.contextInitialized(mockServletContextEvent);
    assertTrue(ConnectionPool.isInitialized(), "Pool should be initialized");

    listener.contextDestroyed(mockServletContextEvent);
    assertFalse(
        ConnectionPool.isInitialized(), "Pool should not be initialized after contextDestroyed");
    assertFalse(
        MongoDatabase.isInitialized(), "MongoDB should not be initialized after contextDestroyed");
  }

  @Test
  @DisplayName("contextInitialized should handle missing config gracefully")
  public void testInitializationFailureHandling() throws IOException {
    TestProperties.deleteMysqlResource();
    ConnectionPool.reset();

    listener.contextInitialized(mockServletContextEvent);

    assertFalse(
        ConnectionPool.isInitialized(), "Pool should not be initialized when config is missing");

    TestProperties.createMysqlResource();
  }

  @Test
  @DisplayName("contextDestroyed without prior init should not throw")
  public void testContextDestroyedWithoutInit() {
    assertFalse(ConnectionPool.isInitialized(), "Pool should not be initialized");
    listener.contextDestroyed(mockServletContextEvent);
    assertFalse(ConnectionPool.isInitialized(), "Pool should still not be initialized");
  }
}
