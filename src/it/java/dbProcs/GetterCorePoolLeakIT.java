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
 * Verifies that high-frequency Getter core DB calls return connections to the Hikari pool (no
 * unbounded growth in active connections).
 */
public class GetterCorePoolLeakIT {

  private static final Logger log = LogManager.getLogger(GetterCorePoolLeakIT.class);
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
      log.warn("Database not available for GetterCorePoolLeakIT: {}", e.getMessage());
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
   * The loop is serial, so a non-leaking pool must return active count to the pre-call baseline
   * after every iteration. Asserting equality with baseline (typically 0) detects leaks
   * deterministically — a hardcoded ceiling can still pass while the pool saturates (e.g. with
   * coreMax=20 and Hikari's 5s acquire timeout, callers throw long before active reaches 32).
   */
  @Test
  public void repeatedAuthUserDoesNotExhaustCorePool() {
    requireDatabase();
    int baseline = ConnectionPool.getCoreActiveConnections();
    assertTrue(baseline >= 0);

    for (int i = 0; i < 500; i++) {
      Getter.authUser(applicationRoot, "nonexistent_pool_leak_user_" + i, "wrongpassword");
    }

    int active = ConnectionPool.getCoreActiveConnections();
    log.debug(
        "GetterCorePoolLeakIT: baseline active={}, after 500 authUser calls active={}",
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
