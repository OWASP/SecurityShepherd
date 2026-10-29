package servlets.module.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dbProcs.ConnectionPool;
import dbProcs.Getter;
import java.io.IOException;
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
   * Pool cap is 3; 4 threads run concurrently. Each call holds one connection only for the
   * duration of the query, so the 4th thread waits briefly for a free slot rather than
   * deadlocking. This would have failed with the old doPost code, where bankAuth held conn1
   * while getAccountBalance tried to acquire conn2 from the same 3-connection pool.
   */
  @Test
  public void concurrentBalanceLookupAbovePoolCapDoesNotDeadlock() throws InterruptedException {
    requireDatabase();

    int threads = 4;
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    AtomicInteger failures = new AtomicInteger(0);

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    for (int i = 0; i < threads; i++) {
      executor.submit(
          () -> {
            try {
              start.await();
              DirectObjectBankLogin.getAccountBalance("0", applicationRoot);
            } catch (Exception e) {
              failures.incrementAndGet();
              log.error("Concurrent balance lookup failed: {}", e.getMessage());
            } finally {
              done.countDown();
            }
          });
    }

    start.countDown();
    boolean completed = done.await(30, TimeUnit.SECONDS);
    executor.shutdownNow();

    assertTrue(completed, "Timed out waiting for concurrent balance lookups to finish");
    assertEquals(0, failures.get(), "Some balance lookups failed unexpectedly");
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
