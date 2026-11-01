package listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;

import dbProcs.ConnectionPool;
import dbProcs.MongoDatabase;
import jakarta.servlet.ServletContextEvent;
import java.io.IOException;
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
 * <p>Only database-free behaviour is covered here. Tests that need a live database live in {@code
 * src/it/java/listeners/DatabaseLifecycleListenerIT}, where the {@code integration-tests} CI job
 * provides MariaDB, so they can never pass silently by skipping. See OWASP/SecurityShepherd#1071.
 */
public class DatabaseLifecycleListenerTest {

  private static final Logger log = LogManager.getLogger(DatabaseLifecycleListenerTest.class);

  @Mock private ServletContextEvent mockServletContextEvent;

  private DatabaseLifecycleListener listener;

  @BeforeAll
  public static void setupClass() throws IOException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.createMongoResource();
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
