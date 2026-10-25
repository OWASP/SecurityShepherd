package dbProcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testUtils.TestProperties;

/**
 * coreSchema.sql must give core and backup, and every table in them, the same character set and
 * collation on every server. Left to the server default they changed between MariaDB 10.6 (latin1
 * for backup) and 12.3 (utf8mb4_uca1400_ai_ci). A table whose collation differs from the schema's
 * breaks every stored procedure that compares one of its columns with a parameter ("Illegal mix of
 * collations").
 */
public class CoreSchemaCharsetIT {

  private static final Logger log = LogManager.getLogger(CoreSchemaCharsetIT.class);

  @BeforeAll
  public static void setup() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.ensureSchemaReady(log);
  }

  @Test
  public void coreSchema_IsUtf8mb4GeneralCi() throws IOException, SQLException {
    assertSchemaCharset("core");
  }

  @Test
  public void backupSchema_IsUtf8mb4GeneralCi() throws IOException, SQLException {
    assertSchemaCharset("backup");
  }

  @Test
  public void coreAndBackupTables_AreUtf8mb4GeneralCi() throws IOException, SQLException {
    try (Connection conn = Database.getDatabaseConnection(null);
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_COLLATION FROM information_schema.TABLES"
                    + " WHERE TABLE_SCHEMA IN ('core', 'backup') AND TABLE_TYPE = 'BASE TABLE'");
        ResultSet rs = ps.executeQuery()) {
      int tables = 0;
      while (rs.next()) {
        tables++;
        assertEquals(
            "utf8mb4_general_ci",
            rs.getString(3),
            "collation of " + rs.getString(1) + "." + rs.getString(2));
      }
      assertTrue(tables > 0, "no tables found in core or backup");
    }
  }

  @Test
  public void procedureComparingColumnWithParameter_Runs() throws IOException, SQLException {
    // Other ITs reseed core.users, so don't rely on any row: the collation check happens when the
    // statement runs, whether or not a row matches
    try (Connection conn = Database.getDatabaseConnection(null);
        CallableStatement cs = conn.prepareCall("CALL core.userFind(?)")) {
      cs.setString(1, "noSuchUser");
      try (ResultSet rs = cs.executeQuery()) {
        assertFalse(rs.next(), "userFind found a user that does not exist");
      }
    }
  }

  private static void assertSchemaCharset(String schema) throws IOException, SQLException {
    try (Connection conn = Database.getDatabaseConnection(null);
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME"
                    + " FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = ?")) {
      ps.setString(1, schema);
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "schema " + schema + " not found");
        assertEquals("utf8mb4", rs.getString(1));
        assertEquals("utf8mb4_general_ci", rs.getString(2));
      }
    }
  }
}
