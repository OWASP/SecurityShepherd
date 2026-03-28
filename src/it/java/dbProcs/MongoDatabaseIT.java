package dbProcs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

  @BeforeEach
  public void setupEach() {
    MongoDatabase.resetInstance();
  }

  @AfterEach
  public void teardownEach() {
    MongoDatabase.resetInstance();
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
      "Fongo 2.1.0 is incompatible with mongo-java-driver 3.12.14 (NPE in"
          + " createOperationExecutor)")
  public void getMongoDatabase_ShouldReturnTypeDB() {}

  @Test
  @DisplayName("Read properties file for db name")
  @Disabled(
      "Fongo 2.1.0 is incompatible with mongo-java-driver 3.12.14 (NPE in"
          + " createOperationExecutor)")
  public void getMongoDatabase_ReadDbName() {}

  // ============= Singleton Pattern Tests =============

  @Test
  @DisplayName("Should return same MongoClient instance on multiple calls (singleton)")
  public void getMongoDbConnection_ShouldReturnSingletonInstance() {
    MongoClient client1 = MongoDatabase.getMongoDbConnection(null);
    MongoClient client2 = MongoDatabase.getMongoDbConnection(null);

    assertNotNull(client1, "First MongoClient should not be null");
    assertNotNull(client2, "Second MongoClient should not be null");
    assertSame(client1, client2, "Multiple calls should return the same MongoClient instance");
  }

  @Test
  @DisplayName("Should return same MongoClient for same credentials (singleton)")
  public void getMongoDbConnection_WithCredentials_ShouldReturnSingletonPerCredential() {
    MongoCredential credential1 =
        MongoCredential.createScramSha1Credential("user1", "db1", "pass1".toCharArray());
    MongoCredential credential2 =
        MongoCredential.createScramSha1Credential("user1", "db1", "pass1".toCharArray());

    MongoClient client1 = MongoDatabase.getMongoDbConnection(null, credential1);
    MongoClient client2 = MongoDatabase.getMongoDbConnection(null, credential2);

    assertNotNull(client1, "First MongoClient should not be null");
    assertNotNull(client2, "Second MongoClient should not be null");
    assertSame(client1, client2, "Same credentials should return the same MongoClient instance");
  }

  @Test
  @DisplayName("Should track initialization state correctly")
  public void isInitialized_ShouldTrackState() {
    assertFalse(MongoDatabase.isInitialized(), "Should not be initialized before first use");

    MongoDatabase.getMongoDbConnection(null);

    assertTrue(MongoDatabase.isInitialized(), "Should be initialized after first use");

    MongoDatabase.resetInstance();
    assertFalse(MongoDatabase.isInitialized(), "Should not be initialized after reset");
  }

  @Test
  @DisplayName("Should handle concurrent singleton initialization safely")
  public void getMongoDbConnection_ShouldBeThreadSafe() throws InterruptedException {
    final int numThreads = 10;
    final CountDownLatch startLatch = new CountDownLatch(1);
    final CountDownLatch doneLatch = new CountDownLatch(numThreads);
    final AtomicReference<MongoClient> firstClient = new AtomicReference<>();
    final java.util.concurrent.atomic.AtomicBoolean allSame =
        new java.util.concurrent.atomic.AtomicBoolean(true);

    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    for (int i = 0; i < numThreads; i++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              MongoClient client = MongoDatabase.getMongoDbConnection(null);

              if (!firstClient.compareAndSet(null, client)) {
                if (firstClient.get() != client) {
                  allSame.set(false);
                }
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              doneLatch.countDown();
            }
          });
    }

    startLatch.countDown();

    boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    assertTrue(completed, "All threads should complete within timeout");
    assertTrue(allSame.get(), "All threads should get the same MongoClient instance");
  }

  @Test
  @DisplayName("Shutdown should close all connections")
  public void shutdown_ShouldCloseConnections() {
    MongoDatabase.getMongoDbConnection(null);
    assertTrue(MongoDatabase.isInitialized(), "Should be initialized");

    MongoDatabase.shutdown();

    assertFalse(MongoDatabase.isInitialized(), "Should not be initialized after shutdown");
  }

  @Test
  @DisplayName("Reset should be equivalent to shutdown")
  public void resetInstance_ShouldBeEquivalentToShutdown() {
    MongoDatabase.getMongoDbConnection(null);
    assertTrue(MongoDatabase.isInitialized(), "Should be initialized");

    MongoDatabase.resetInstance();

    assertFalse(MongoDatabase.isInitialized(), "Should not be initialized after reset");
  }
}
