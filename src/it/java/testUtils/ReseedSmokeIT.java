package testUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dbProcs.Database;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.CheatSheetStatus;
import utils.CountdownHandler;
import utils.FeedbackStatus;
import utils.ModulePlan;
import utils.OpenRegistration;
import utils.ScoreboardStatus;

/**
 * Smoke test guarding the contract of {@link TestProperties#reseedTestData()}. If a new mutable
 * table or cached setting is introduced and not wired into reseedTestData, the symptom would
 * otherwise be order-dependent flakes in unrelated IT classes. This test fails loudly instead.
 *
 * <p>Strategy: dirty every piece of state that reseedTestData is responsible for, call it, then
 * assert each invariant.
 */
public class ReseedSmokeIT {

  private static final Logger log = LogManager.getLogger(ReseedSmokeIT.class);

  private static final long USERS_SEQ_INITIAL = 282475249L;
  private static final long CHEATSHEET_SEQ_INITIAL = 282475299L;
  private static final long CLASS_SEQ_INITIAL = 282475249L;
  private static final long MODULES_SEQ_INITIAL = 282475576L;

  @BeforeAll
  public static void setUpAndDirtyState() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();

    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();

    dirtyEveryThingReseedShouldClean();

    TestProperties.reseedTestData();
  }

  private static void dirtyEveryThingReseedShouldClean() throws SQLException {
    try (Connection conn = Database.getCoreConnection("");
        Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(
          "INSERT INTO class (classId, className, classYear) "
              + "VALUES ('smoke-class-id', 'smoke', '2026')");
      stmt.executeUpdate(
          "INSERT INTO users (userId, classId, userName, userPass, userRole) "
              + "VALUES ('smoke-user-id', 'smoke-class-id', 'smokeUser', 'x', 'player')");
      stmt.executeUpdate(
          "INSERT INTO results (userId, moduleId, startTime) "
              + "SELECT 'smoke-user-id', moduleId, NOW() FROM modules LIMIT 1");
      stmt.executeUpdate(
          "INSERT INTO medals (classId, moduleId) "
              + "SELECT 'smoke-class-id', moduleId FROM modules LIMIT 1");

      stmt.executeUpdate("UPDATE modules SET moduleStatus = 'closed'");

      stmt.executeUpdate("UPDATE sequence SET currVal = 999999999 WHERE tableName = 'users'");
      stmt.executeUpdate("UPDATE sequence SET currVal = 999999999 WHERE tableName = 'cheatSheet'");
      stmt.executeUpdate("UPDATE sequence SET currVal = 999999999 WHERE tableName = 'class'");
      stmt.executeUpdate("UPDATE sequence SET currVal = 999999999 WHERE tableName = 'modules'");
    }

    CheatSheetStatus.enableForAll();
    ScoreboardStatus.disableScoreboard();
    FeedbackStatus.setEnabled();
    OpenRegistration.enable();
    ModulePlan.setOpenFloor();
    CountdownHandler.setStartTime(LocalDateTime.now().minusDays(1));
    CountdownHandler.setLockTime(LocalDateTime.now().plusDays(1));
    CountdownHandler.setEndTime(LocalDateTime.now().plusDays(2));
  }

  @Test
  public void reseed_clearsMutableTables() throws IOException, SQLException {
    assertEquals(0, rowCount("users"));
    assertEquals(0, rowCount("class"));
    assertEquals(0, rowCount("results"));
    assertEquals(0, rowCount("medals"));
  }

  @Test
  public void reseed_keepsModuleCatalogIntactAndOpen() throws IOException, SQLException {
    long total = rowCount("modules");
    assertTrue(total > 0, "module catalog should be non-empty after reseed");
    assertEquals(
        total,
        rowCountWhere("modules", "moduleStatus = 'open'"),
        "all modules should be 'open' after reseed");
  }

  @Test
  public void reseed_restoresSequenceCounters() throws IOException, SQLException {
    assertEquals(USERS_SEQ_INITIAL, sequenceValue("users"));
    assertEquals(CHEATSHEET_SEQ_INITIAL, sequenceValue("cheatSheet"));
    assertEquals(CLASS_SEQ_INITIAL, sequenceValue("class"));
    assertEquals(MODULES_SEQ_INITIAL, sequenceValue("modules"));
  }

  @Test
  public void reseed_restoresCachedSettingsToDefaults() {
    assertFalse(CheatSheetStatus.isEnabledAtAll(), "cheat sheet should be disabled by default");
    assertTrue(ScoreboardStatus.isScoreboardEnabled(), "scoreboard should be open by default");
    assertTrue(FeedbackStatus.isDisabled(), "feedback should be disabled by default");
    assertTrue(OpenRegistration.isDisabled(), "open registration should be disabled by default");
    assertTrue(ModulePlan.isIncrementalFloor(), "module plan should be incremental by default");
  }

  private static long rowCount(String table) throws IOException, SQLException {
    return scalarLong("SELECT COUNT(*) FROM " + table);
  }

  private static long rowCountWhere(String table, String where) throws IOException, SQLException {
    return scalarLong("SELECT COUNT(*) FROM " + table + " WHERE " + where);
  }

  private static long sequenceValue(String tableName) throws IOException, SQLException {
    try (Connection conn = Database.getCoreConnection("");
        java.sql.PreparedStatement ps =
            conn.prepareStatement("SELECT currVal FROM sequence WHERE tableName = ?")) {
      ps.setString(1, tableName);
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "sequence row missing for " + tableName);
        return rs.getLong(1);
      }
    }
  }

  private static long scalarLong(String sql) throws IOException, SQLException {
    try (Connection conn = Database.getCoreConnection("");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      assertTrue(rs.next(), "no rows returned for: " + sql);
      return rs.getLong(1);
    }
  }
}
