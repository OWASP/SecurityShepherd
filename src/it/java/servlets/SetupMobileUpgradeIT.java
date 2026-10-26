package servlets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dbProcs.Database;
import dbProcs.Getter;
import dbProcs.Setter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testUtils.TestProperties;

/**
 * An existing database keeps the Mobile Insecure Data Storage 2 and 3 and Mobile Poor
 * Authentication 2 modules that coreSchema.sql no longer creates, and their lang pointers are gone
 * from moduleNames.properties. The "upgrade-mobile" Setup option must remove them and rename the
 * surviving mobile modules, and must be safe to run again.
 */
public class SetupMobileUpgradeIT {

  private static final Logger log = LogManager.getLogger(SetupMobileUpgradeIT.class);
  private static final String APPLICATION_ROOT = "";
  private static final String USER_NAME = "mobileUpgradeUser";

  private static final String IDS_1 = "307f78f18fd6a87e50ed6705231a9f24cd582574";

  /** Removed modules: moduleId -> lang pointer left behind in an old database. */
  private static final Map<String, String> REMOVED =
      Map.of(
          "da3de2e556494a9c2fb7308a98454cf55f3a4911", "insecure.data.storage.2",
          "cb7d696bdf88899e8077063d911fc8da14176702", "insecure.data.storage.3",
          "064e28ea4b2f7708b8cb4495d9db1a5e05decdb8", "poor.authentication.2");

  /** Renamed modules: moduleId -> {old name, new name}. */
  private static final Map<String, String[]> RENAMED =
      Map.of(
          "53a53a66cb3bf3e4c665c442425ca90e29536edd",
          new String[] {"Insecure Data Storage", "Mobile Insecure Data Storage"},
          "335440fef02d19259254ed88293b62f31cccdd41",
          new String[] {"Client Side Injection", "Mobile Client Side Injection"},
          "0cdd1549e7c74084d7059ce748b93ef657b44457",
          new String[] {"Poor Authentication", "Mobile Poor Authentication"},
          IDS_1,
          new String[] {"Insecure Data Storage 1", "Mobile Insecure Data Storage 1"},
          "a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4",
          new String[] {"Client Side Injection 1", "Mobile Client Side Injection 1"},
          "e635fce334aa61fdaa459c21c286d6332eddcdd3",
          new String[] {"Client Side Injection 2", "Mobile Client Side Injection 2"},
          "ba6e65e4881c8499b5e53eb33b5be6b5d0f1fb2c",
          new String[] {"Poor Authentication 1", "Mobile Poor Authentication 1"});

  private static String userId;

  @BeforeAll
  public static void setup() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();
    Setter.userCreate(
        APPLICATION_ROOT, null, USER_NAME, USER_NAME, "player", USER_NAME + "@test.com", false);
    userId = Getter.authUser(APPLICATION_ROOT, USER_NAME, USER_NAME)[0];
  }

  @AfterAll
  public static void cleanUp() throws IOException, SQLException {
    // Leave the database as coreSchema.sql builds it for the ITs that follow
    Setup.executeUpdateScript(Setup.UPGRADE_MOBILE_MODULES_SCRIPT);
    TestProperties.reseedTestData();
  }

  /** Puts the database in the state an install from before the consolidation is in. */
  @BeforeEach
  public void seedOldDatabase() throws SQLException {
    try (Connection conn = Database.getCoreConnection(APPLICATION_ROOT)) {
      for (Map.Entry<String, String[]> rename : RENAMED.entrySet()) {
        execute(
            conn,
            "UPDATE modules SET moduleName = ? WHERE moduleId = ?",
            rename.getValue()[0],
            rename.getKey());
      }
      for (Map.Entry<String, String> removed : REMOVED.entrySet()) {
        String id = removed.getKey();
        execute(conn, "DELETE FROM modules WHERE moduleId = ?", id);
        execute(
            conn,
            "INSERT INTO modules (moduleId, moduleName, moduleNameLangPointer, moduleType,"
                + " moduleCategory, moduleCategoryLangPointer, moduleResult, moduleHash,"
                + " moduleStatus, incrementalRank, scoreValue, hardcodedKey, isUnsafe)"
                + " SELECT ?, ?, ?, moduleType, moduleCategory, moduleCategoryLangPointer,"
                + " ?, ?, 'open', incrementalRank, scoreValue, hardcodedKey, isUnsafe"
                + " FROM modules WHERE moduleId = ?",
            id,
            removed.getValue(),
            removed.getValue(),
            id.substring(0, 20),
            id,
            IDS_1);
        execute(
            conn,
            "INSERT INTO cheatsheet (cheatSheetId, moduleId, createDate, solution)"
                + " VALUES (?, ?, NOW(), ?)",
            id.substring(1) + "0",
            id,
            id + ".solution");
        execute(conn, "INSERT INTO medals (classId, moduleId, scoreBonus) VALUES (NULL, ?, 5)", id);
        execute(
            conn,
            "INSERT INTO results (userId, moduleId, startTime) VALUES (?, ?, NOW())",
            userId,
            id);
      }
    }
  }

  @Test
  public void oldDatabase_ChallengeListFailsWithoutUpgrade() {
    assertThrows(
        MissingResourceException.class,
        () -> Getter.getChallenges(APPLICATION_ROOT, userId, new Locale("en_GB")));
  }

  @Test
  public void upgrade_RemovesModulesAndTheirRows() throws IOException, SQLException {
    Setup.executeUpdateScript(Setup.UPGRADE_MOBILE_MODULES_SCRIPT);

    for (String id : REMOVED.keySet()) {
      assertEquals(0, count("modules", id), "modules row for " + id);
      assertEquals(0, count("medals", id), "medals rows for " + id);
      assertEquals(0, count("results", id), "results rows for " + id);
      assertEquals(0, count("cheatsheet", id), "cheatsheet rows for " + id);
    }
  }

  @Test
  public void upgrade_RenamesRemainingMobileModules() throws IOException, SQLException {
    Setup.executeUpdateScript(Setup.UPGRADE_MOBILE_MODULES_SCRIPT);

    for (Map.Entry<String, String[]> rename : RENAMED.entrySet()) {
      assertEquals(
          List.of(rename.getValue()[1]),
          names(rename.getKey()),
          "moduleName for " + rename.getKey());
    }
  }

  @Test
  public void upgrade_LeavesChallengeListLoading() throws IOException, SQLException {
    Setup.executeUpdateScript(Setup.UPGRADE_MOBILE_MODULES_SCRIPT);

    assertDoesNotThrow(() -> Getter.getChallenges(APPLICATION_ROOT, userId, new Locale("en_GB")));
  }

  @Test
  public void upgrade_CanBeRunAgain() throws IOException, SQLException {
    Setup.executeUpdateScript(Setup.UPGRADE_MOBILE_MODULES_SCRIPT);
    Setup.executeUpdateScript(Setup.UPGRADE_MOBILE_MODULES_SCRIPT);

    for (String id : REMOVED.keySet()) {
      assertEquals(0, count("modules", id), "modules row for " + id);
    }
    for (Map.Entry<String, String[]> rename : RENAMED.entrySet()) {
      assertEquals(List.of(rename.getValue()[1]), names(rename.getKey()));
    }
  }

  @Test
  public void upgrade_OnAlreadyCurrentDatabase_ChangesNothing() throws IOException, SQLException {
    Setup.executeUpdateScript(Setup.UPGRADE_MOBILE_MODULES_SCRIPT);
    int modules = count("modules", null);

    Setup.executeUpdateScript(Setup.UPGRADE_MOBILE_MODULES_SCRIPT);

    assertEquals(modules, count("modules", null));
  }

  private static void execute(Connection conn, String sql, String... params) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      for (int i = 0; i < params.length; i++) {
        ps.setString(i + 1, params[i]);
      }
      ps.executeUpdate();
    }
  }

  private static int count(String table, String moduleId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM " + table + (moduleId == null ? "" : " WHERE moduleId = ?");
    try (Connection conn = Database.getCoreConnection(APPLICATION_ROOT);
        PreparedStatement ps = conn.prepareStatement(sql)) {
      if (moduleId != null) {
        ps.setString(1, moduleId);
      }
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getInt(1);
      }
    }
  }

  private static List<String> names(String moduleId) throws SQLException {
    try (Connection conn = Database.getCoreConnection(APPLICATION_ROOT);
        PreparedStatement ps =
            conn.prepareStatement("SELECT moduleName FROM modules WHERE moduleId = ?")) {
      ps.setString(1, moduleId);
      try (ResultSet rs = ps.executeQuery()) {
        List<String> found = new java.util.ArrayList<>();
        while (rs.next()) {
          found.add(rs.getString(1));
        }
        return found;
      }
    }
  }
}
