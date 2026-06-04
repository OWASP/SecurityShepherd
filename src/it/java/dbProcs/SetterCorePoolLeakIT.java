package dbProcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
   * safely. Pre-fix, the success path called Database.closeConnection and returned the connection,
   * but any thrown SQLException (or early return) skipped that line and leaked the connection —
   * exactly the failure mode the try-with-resources conversion fixes.
   *
   * <p>The loop is serial, so a non-leaking pool must return active count to the pre-call baseline
   * after every iteration. Asserting equality with baseline (typically 0) detects leaks
   * deterministically — a hardcoded ceiling can still pass while the pool saturates.
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
    assertEquals(
        baseline,
        active,
        "Core pool active connections should return to baseline after a serial loop; got "
            + active
            + " (baseline "
            + baseline
            + ")");
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
    assertEquals(
        baseline,
        active,
        "Core pool active connections should return to baseline after a serial loop; got "
            + active
            + " (baseline "
            + baseline
            + ")");
  }
}
