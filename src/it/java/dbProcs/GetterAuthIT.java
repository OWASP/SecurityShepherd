package dbProcs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * Integration tests for Getter.authUser() fail-fast checks: suspended users and SSO users should be
 * rejected before Argon2 verification.
 */
public class GetterAuthIT {

  private static final Logger log = LogManager.getLogger(GetterAuthIT.class);
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
      log.warn("Database not available for GetterAuthIT: {}", e.getMessage());
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
  public void suspendedUserIsRejected() throws SQLException {
    requireDatabase();
    String userName = "authSuspendedUser";

    // Create user and verify they can authenticate
    Setter.userCreate(
        applicationRoot, null, userName, userName, "player", userName + "@test.com", false);
    String[] user = Getter.authUser(applicationRoot, userName, userName);
    assertNotNull(user, "User should be able to authenticate before suspension");

    String userId = user[0];

    // Suspend and verify rejection
    assertTrue(Setter.suspendUser(applicationRoot, userId, 10), "Could not suspend user");
    assertNull(
        Getter.authUser(applicationRoot, userName, userName),
        "Suspended user should not be able to authenticate");

    // Unsuspend and verify access restored
    assertTrue(Setter.unSuspendUser(applicationRoot, userId), "Could not unsuspend user");
    assertNotNull(
        Getter.authUser(applicationRoot, userName, userName),
        "User should be able to authenticate after unsuspension");
  }

  @Test
  public void ssoUserIsRejectedFromPasswordLogin() {
    requireDatabase();
    String userName = "authRejectSSOUser Lastname";
    String ssoName = "authrejectssouser@example.com";

    // Create an SSO user
    String[] user = Getter.authUserSSO(applicationRoot, null, userName, ssoName, "player");
    assertNotNull(user, "Could not create SSO user");

    // SSO user should not be able to authenticate with password login
    assertNull(
        Getter.authUser(applicationRoot, userName, "anyPassword"),
        "SSO user should not be able to authenticate via password login");
  }
}
