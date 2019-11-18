package dbProcs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.fakemongo.Fongo;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

import testUtils.TestProperties;

public class MongoDatabaseTest {

    private static Fongo fongo = new Fongo("Unit Test");
    private static MongoClient mongoClient;
    private static String TEST_PATH = "mongo_challenge_test";

    private static org.apache.log4j.Logger log = Logger.getLogger(MongoDatabaseTest.class);

    @BeforeAll
    public static void initAll() throws IOException
    {
		TestProperties.createMysqlResource();

        mongoClient = fongo.getMongo();
        TestProperties.setTestPropertiesFileDirectory(log);
    }

    @Test
    @DisplayName("Should Return Type MongoCredentials")
    public void getMongoChallengeCredentials_ShouldReturnTypeMongoCredentials() throws IOException
    {
        assertThat(MongoDatabase.getMongoChallengeCredentials(null, TEST_PATH),
                instanceOf(MongoCredential.class));
    }

    @Test
    @DisplayName("Should read properties file for mongo challenge credentials")
    public void getMongoChallengeCredentials_ShouldReadPropertiesFileForCreds() throws IOException
    {
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
    public void getMongoChallengeCollName_ShouldReturnTypeString()
    {
        assertThat(MongoDatabase.getMongoChallengeCollName(null, TEST_PATH),
                instanceOf(String.class));
    }

    @Test
    @DisplayName("Should read properties file with connection details to challenge")
    public void getMongoChallengeCollName_ReadPropertiesFile()
    {
        MongoDatabase.getMongoChallengeCollName(null, TEST_PATH);

    }

    @Test
    @DisplayName("Should return a MongoClient instance")
    public void getMongoDbConnection_ShouldReturnTypeMongoClient()
    {
        MongoCredential credential =
                MongoCredential.createScramSha1Credential("test", "test", "test".toCharArray());
        assertThat(MongoDatabase.getMongoDbConnection(null), instanceOf(MongoClient.class));
        assertThat(MongoDatabase.getMongoDbConnection(null, credential), instanceOf(MongoClient.class));
    }


    @Test
    @DisplayName("Must return type (Mongo) DB")
    public void getMongoDatabase_ShouldReturnTypeDB()
    {
        assertThat(MongoDatabase.getMongoDatabase(mongoClient), instanceOf(DB.class));
    }

    @Test
    @DisplayName("Read properties file for db name")
    public void getMongoDatabase_ReadDbName()
    {
        DB db = MongoDatabase.getMongoDatabase(mongoClient);
        assertEquals("test_shepherdGames", db.getName());
    }

}