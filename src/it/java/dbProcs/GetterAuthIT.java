package dbProcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;
import org.mockito.MockedStatic;
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

  /** authUserSSO fills the result array the same way as authUser (#820) */
  @Test
  public void ssoLoginResultFollowsAuthUserContract() {
    requireDatabase();
    String userName = "authContractSSOUser Lastname";
    String ssoName = "authcontractssouser@example.com";

    String[] user = Getter.authUserSSO(applicationRoot, null, userName, ssoName, "player");

    assertNotNull(user, "SSO login did not succeed");
    assertEquals("player", user[2], "role");
    assertEquals("false", user[3], "tempPassword: SSO users have no password to change");
    assertTrue(
        "true".equals(user[5]) || "false".equals(user[5]),
        "tempUsername should be a boolean string, was " + user[5]);
  }

  @Test
  public void suspensionUsesDatabaseClockAcrossTimezones() throws SQLException {
    requireDatabase();
    String userName = "authTimezoneSuspendedUser";
    Setter.userCreate(
        applicationRoot, null, userName, userName, "player", userName + "@test.com", false);
    String[] user = Getter.authUser(applicationRoot, userName, userName);
    assertNotNull(user);

    TimeZone original = TimeZone.getDefault();
    try (Connection databaseConnection = Database.getCoreConnection(applicationRoot);
        Statement statement = databaseConnection.createStatement()) {
      String originalDatabaseTimeZone;
      try (ResultSet zone = statement.executeQuery("SELECT @@session.time_zone")) {
        zone.next();
        originalDatabaseTimeZone = zone.getString(1);
      }
      try {
        statement.execute("SET time_zone = '-08:00'");
        try (CallableStatement suspend = databaseConnection.prepareCall("CALL suspendUser(?, ?)")) {
          suspend.setString(1, user[0]);
          suspend.setInt(2, 10);
          suspend.execute();
        }

        // Keep this session open for authUser so its SQL reads the same DB-local time
        // that the suspension procedure wrote.
        Connection sharedConnection = mock(Connection.class, delegatesTo(databaseConnection));
        doNothing().when(sharedConnection).close();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        try (MockedStatic<Database> database =
            mockStatic(Database.class, withSettings().mockMaker(MockMakers.INLINE))) {
          database
              .when(() -> Database.getCoreConnection(applicationRoot))
              .thenReturn(sharedConnection);
          assertNull(
              Getter.authUser(applicationRoot, userName, userName),
              "A DB-local suspension must remain active under a different JVM timezone");
        }
      } finally {
        try (PreparedStatement restore = databaseConnection.prepareStatement("SET time_zone = ?")) {
          restore.setString(1, originalDatabaseTimeZone);
          restore.execute();
        }
      }
    } finally {
      TimeZone.setDefault(original);
    }
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
