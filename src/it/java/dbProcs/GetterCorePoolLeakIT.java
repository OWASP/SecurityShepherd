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
 * Verifies that high-frequency Getter core DB calls return connections to the Hikari pool (no
 * unbounded growth in active connections).
 */
public class GetterCorePoolLeakIT {

  private static final Logger log = LogManager.getLogger(GetterCorePoolLeakIT.class);
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
    assertTrue(
        active <= MAX_ALLOWED_ACTIVE,
        "Core pool active connections should stay bounded; got " + active);
  }
}
