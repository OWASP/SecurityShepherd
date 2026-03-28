package dbProcs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testUtils.TestProperties;

public class MongoDatabaseIT {

  private static String TEST_PATH = "mongo_challenge_test";

  private static final Logger log = LogManager.getLogger(MongoDatabaseIT.class);

  @BeforeAll
  public static void initAll() throws IOException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.createMongoResource();
  }

  @Test
  @DisplayName("Should Return Type MongoCredentials")
  public void getMongoChallengeCredentials_ShouldReturnTypeMongoCredentials() throws IOException {
    assertInstanceOf(
        MongoCredential.class, MongoDatabase.getMongoChallengeCredentials(null, TEST_PATH));
  }

  @Test
  @DisplayName("Should read properties file for mongo challenge credentials")
  public void getMongoChallengeCredentials_ShouldReadPropertiesFileForCreds() throws IOException {
    MongoCredential creds = MongoDatabase.getMongoChallengeCredentials(null, TEST_PATH);

    String username = creds.getUserName();
    char[] password = creds.getPassword();
    String databasename = creds.getSource();

    assertEquals("test_user", username);
    assertArrayEquals("test_password".toCharArray(), password);
    assertEquals("test_dbname", databasename);
  }

  @Test
  @DisplayName("Should Return Type String")
  public void getMongoChallengeCollName_ShouldReturnTypeString() {
    String collName = MongoDatabase.getMongoChallengeCollName(null, TEST_PATH);
    assertNotNull(collName);
    assertInstanceOf(String.class, collName);
  }

  @Test
  @DisplayName("Should read properties file with connection details to challenge")
  public void getMongoChallengeCollName_ReadPropertiesFile() {
    String collName = MongoDatabase.getMongoChallengeCollName(null, TEST_PATH);
    assertEquals("test_collection", collName);
  }

  @Test
  @DisplayName("Should return a MongoClient instance")
  public void getMongoDbConnection_ShouldReturnTypeMongoClient() {
    MongoCredential credential =
        MongoCredential.createScramSha1Credential("test", "test", "test".toCharArray());
    assertInstanceOf(MongoClient.class, MongoDatabase.getMongoDbConnection(null));
    assertInstanceOf(MongoClient.class, MongoDatabase.getMongoDbConnection(null, credential));
  }

  @Test
  @DisplayName("Must return type (Mongo) DB")
  @Disabled(
      "Fongo 2.1.0 is incompatible with mongo-java-driver 3.12.14 (NPE in createOperationExecutor)")
  public void getMongoDatabase_ShouldReturnTypeDB() {}

  @Test
  @DisplayName("Read properties file for db name")
  @Disabled(
      "Fongo 2.1.0 is incompatible with mongo-java-driver 3.12.14 (NPE in createOperationExecutor)")
  public void getMongoDatabase_ReadDbName() {}
}
