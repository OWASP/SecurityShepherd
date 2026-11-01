package listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dbProcs.ConnectionPool;
import dbProcs.MongoDatabase;
import jakarta.servlet.ServletContextEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import servlets.Setup;
import testUtils.TestProperties;

/**
 * Integration tests for the DatabaseLifecycleListener class.
 *
 * <p>These tests need a live MariaDB, which the {@code integration-tests} CI job provides. They
 * fail loudly when the database is unreachable instead of skipping, so a broken test database can
 * never pass the build silently. See OWASP/SecurityShepherd#1071.
 */
public class DatabaseLifecycleListenerIT {

  private static final Logger log = LogManager.getLogger(DatabaseLifecycleListenerIT.class);

  @Mock private ServletContextEvent mockServletContextEvent;

  private DatabaseLifecycleListener listener;

  @BeforeAll
  public static void setupClass() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.createMongoResource();

    // Fail loudly here: integration runs must have a database, never skip silently.
    ConnectionPool.initialize();
    try (Connection conn = ConnectionPool.getConnection()) {
      log.info("Database is available - running integration test suite");
    } finally {
      ConnectionPool.reset();
    }
  }

  @AfterAll
  public static void tearDownClass() {
    ConnectionPool.shutdown();
    Setup.resetInstalledCache();
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

  @Test
  @DisplayName("contextInitialized should initialize the connection pool")
  public void testContextInitialized() {
    assertFalse(
        ConnectionPool.isInitialized(), "Pool should not be initialized before contextInitialized");
    listener.contextInitialized(mockServletContextEvent);
    assertTrue(
        ConnectionPool.isInitialized(), "Pool should be initialized after contextInitialized");
  }

  @Test
  @DisplayName("contextDestroyed should shut down all pools")
  public void testContextDestroyed() {
    listener.contextInitialized(mockServletContextEvent);
    assertTrue(ConnectionPool.isInitialized(), "Pool should be initialized");

    listener.contextDestroyed(mockServletContextEvent);
    assertFalse(
        ConnectionPool.isInitialized(), "Pool should not be initialized after contextDestroyed");
    assertFalse(
        MongoDatabase.isInitialized(), "MongoDB should not be initialized after contextDestroyed");
  }
}
