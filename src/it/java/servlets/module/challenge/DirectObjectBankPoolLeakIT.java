package servlets.module.challenge;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dbProcs.ConnectionPool;
import dbProcs.Getter;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testUtils.TestProperties;

/**
 * Verifies that {@link DirectObjectBankLogin#getAccountBalance} returns its connection to the
 * challenge pool even when it throws.
 *
 * <p>Each challenge schema has its own pool capped at 3 connections with a short acquire timeout.
 * If a failed call leaked its connection, the pool would be exhausted by the fourth call and that
 * call would fail with a pool timeout instead of the expected "No Rows Found" SQLException.
 */
public class DirectObjectBankPoolLeakIT {

  private static final Logger log = LogManager.getLogger(DirectObjectBankPoolLeakIT.class);
  private static final String applicationRoot = "";
  private static final String nonexistentAccount = "pool_leak_nonexistent_account";

  /** Comfortably above the 3-connection challenge pool cap, so a per-call leak cannot go unseen. */
  private static final int CALLS = 10;

  private static boolean databaseAvailable = false;

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
      log.warn("Database not available for DirectObjectBankPoolLeakIT: {}", e.getMessage());
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
   * The calls are serial, so a non-leaking pool hands the same connection back every time. Each
   * call must fail with the "No Rows Found" SQLException thrown by getAccountBalance itself, not
   * with a pool timeout.
   */
  @Test
  public void repeatedFailedBalanceLookupDoesNotExhaustChallengePool() {
    requireDatabase();

    for (int i = 0; i < CALLS; i++) {
      final int call = i + 1;
      SQLException thrown =
          assertThrows(
              SQLException.class,
              () -> DirectObjectBankLogin.getAccountBalance(nonexistentAccount, applicationRoot),
              "Call " + call + " should fail with the expected SQLException");
      assertNotNull(thrown.getMessage(), "Call " + call + " threw an SQLException with no message");
      assertTrue(
          thrown.getMessage().contains("No Rows Found"),
          "Call "
              + call
              + " failed for the wrong reason (a pool timeout means a connection leaked): "
              + thrown.getMessage());
    }
  }
}
