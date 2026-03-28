package listeners;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dbProcs.ConnectionPool;
import dbProcs.MongoDatabase;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletContextEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import testUtils.TestProperties;

/**
 * Unit tests for the DatabaseLifecycleListener class.
 *
 * Note: Tests that require actual database connectivity will be skipped if
 * the database is not available. Run with a proper database setup for full coverage.
 */
public class DatabaseLifecycleListenerTest {

  private static final Logger log = LogManager.getLogger(DatabaseLifecycleListenerTest.class);
  private static boolean databaseAvailable = false;

  @Mock
  private ServletContextEvent mockServletContextEvent;

  private DatabaseLifecycleListener listener;

  @BeforeClass
  public static void setupClass() throws IOException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.createMongoResource();

    // Check if database is available
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

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.openMocks(this);

    // Reset pool state FIRST (before creating listener)
    ConnectionPool.reset();
    MongoDatabase.resetInstance();

    // Ensure properties files exist before any pool operations
    TestProperties.createMysqlResource();
    TestProperties.createMongoResource();

    // Create listener after properties exist
    listener = new DatabaseLifecycleListener();
  }

  /**
   * Helper method to skip tests that require database connectivity.
   */
  private void requireDatabase() {
    if (!databaseAvailable) {
      log.info("Skipping test - database not available");
    }
    Assume.assumeTrue(databaseAvailable);
  }

  @Test
  public void testContextInitialized() {
    requireDatabase();

    // Verify pool is not initialized before
    assertFalse("Pool should not be initialized before contextInitialized",
        ConnectionPool.isInitialized());

    // Call contextInitialized
    listener.contextInitialized(mockServletContextEvent);

    // Verify pool is now initialized
    assertTrue("Pool should be initialized after contextInitialized",
        ConnectionPool.isInitialized());
  }

  @Test
  public void testContextDestroyed() {
    requireDatabase();

    // First initialize the pools
    listener.contextInitialized(mockServletContextEvent);
    assertTrue("Pool should be initialized", ConnectionPool.isInitialized());

    // Call contextDestroyed
    listener.contextDestroyed(mockServletContextEvent);

    // Verify pools are shut down
    assertFalse("Pool should not be initialized after contextDestroyed",
        ConnectionPool.isInitialized());
    assertFalse("MongoDB should not be initialized after contextDestroyed",
        MongoDatabase.isInitialized());
  }

  @Test
  public void testInitializationFailureHandling() throws IOException {
    // This test verifies that initialization failures are handled gracefully
    // The listener catches exceptions and logs them, allowing app to continue

    // Delete the properties file to simulate a configuration error
    TestProperties.deleteMysqlResource();

    // Reset pool to ensure clean state
    ConnectionPool.reset();

    // The listener should catch the exception internally and not throw
    // (it logs the error but doesn't prevent app startup)
    listener.contextInitialized(mockServletContextEvent);

    // Pool should NOT be initialized because the properties file was missing
    assertFalse("Pool should not be initialized when config is missing",
        ConnectionPool.isInitialized());

    // Restore the properties file for other tests
    TestProperties.createMysqlResource();
  }

  @Test
  public void testContextDestroyedWithoutInit() {
    // This test doesn't require a database - tests that destroy is safe without init

    // Verify pool is not initialized
    assertFalse("Pool should not be initialized", ConnectionPool.isInitialized());

    // Call contextDestroyed without first initializing - should not throw
    listener.contextDestroyed(mockServletContextEvent);

    // Still not initialized
    assertFalse("Pool should still not be initialized", ConnectionPool.isInitialized());
  }
}
