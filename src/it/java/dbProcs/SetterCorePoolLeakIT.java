package dbProcs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testUtils.TestProperties;

/**
 * Verifies that high-frequency Setter core DB calls return connections to the Hikari pool (no
 * unbounded growth in active connections). Mirrors GetterCorePoolLeakIT.
 */
public class SetterCorePoolLeakIT {

  private static final Logger log = LogManager.getLogger(SetterCorePoolLeakIT.class);
  private static boolean databaseAvailable = false;
  private static final String applicationRoot = "";

  /** Must stay within configured core pool max (see database.properties / ConnectionPool). */
  private static final int MAX_ALLOWED_ACTIVE = 32;

  @BeforeAll
  public static void setup() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();
    try {
      ConnectionPool.initialize();
      Getter.getClassCount(applicationRoot);
      databaseAvailable = ConnectionPool.isInitialized();
    } catch (Exception e) {
      log.warn("Database not available for SetterCorePoolLeakIT: {}", e.getMessage());
      databaseAvailable = false;
    }
  }

  @AfterAll
  public static void cleanup() {
    ConnectionPool.shutdown();
  }

  private void requireDatabase() {
    assumeTrue(databaseAvailable, "Database not available");
  }

  /**
   * resetBadSubmission is a representative core write: it borrows a connection, runs a stored
   * procedure, returns. With a nonexistent userId the procedure is a no-op, so we can hammer it
   * safely. Pre-fix, every iteration would leak a connection on the success path's manual
   * closeConnection call (and on every exception path).
   */
  @Test
  public void repeatedResetBadSubmissionDoesNotExhaustCorePool() {
    requireDatabase();
    int baseline = ConnectionPool.getCoreActiveConnections();
    assertTrue(baseline >= 0);

    for (int i = 0; i < 500; i++) {
      Setter.resetBadSubmission(applicationRoot, "nonexistent_pool_leak_user_" + i);
    }

    int active = ConnectionPool.getCoreActiveConnections();
    log.debug(
        "SetterCorePoolLeakIT: baseline active={}, after 500 resetBadSubmission calls active={}",
        baseline,
        active);
    assertTrue(
        active <= MAX_ALLOWED_ACTIVE,
        "Core pool active connections should stay bounded; got " + active);
  }

  /**
   * suspendUser exercises a different code path (the catch-and-return-false branch on most calls
   * since the userId does not exist). Verifies the exception path also returns the connection.
   */
  @Test
  public void repeatedSuspendUserDoesNotExhaustCorePool() {
    requireDatabase();
    int baseline = ConnectionPool.getCoreActiveConnections();
    assertTrue(baseline >= 0);

    for (int i = 0; i < 500; i++) {
      Setter.suspendUser(applicationRoot, "nonexistent_pool_leak_user_" + i, 1);
    }

    int active = ConnectionPool.getCoreActiveConnections();
    log.debug(
        "SetterCorePoolLeakIT: baseline active={}, after 500 suspendUser calls active={}",
        baseline,
        active);
    assertTrue(
        active <= MAX_ALLOWED_ACTIVE,
        "Core pool active connections should stay bounded; got " + active);
  }
}
