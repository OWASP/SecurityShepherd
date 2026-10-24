package dbProcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
 * coreSchema.sql must give core and backup the same character set and collation on every server.
 * Left to the server default, they changed between MariaDB 10.6 (latin1 for backup) and 12.3
 * (utf8mb4_uca1400_ai_ci for both).
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
