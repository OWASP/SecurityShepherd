package dbProcs;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ServerAddress;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to create MongoDb connections <br>
 * <br>
 * This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Paul
 */
public class MongoDatabase {

  private static final Logger log = LogManager.getLogger(MongoDatabase.class);

  /**
   * Method to close a MongoDb connection
   *
   * @param conn The connection to close
   */
  public static void closeConnection(MongoClient conn) {

    conn.close();
  }

  /**
   * Method to get a MongoDb Challenge collection
   *
   * @param ApplicationRoot The running context of the application.
   * @param path The path to the properties file to use for this connection (filtered for path
   *     traversal attacks)
   * @return A MongoDb Collection
   * @throws IOException
   */
  public static MongoCredential getMongoChallengeCredentials(String ApplicationRoot, String path)
      throws IOException {
    // Some over paranoid input validation never hurts.
    Properties prop = new Properties();

    path = path.replaceAll("\\.", "").replaceAll("/", "");
    log.debug("Path = " + path);

    MongoCredential credential;

    String mongo_props =
        new File(
                Objects.requireNonNull(
                        MongoDatabase.class.getResource("/challenges/" + path + ".properties"))
                    .getFile())
            .getAbsolutePath();
    log.debug("Level Properties File = " + path + ".properties");

    try (InputStream mongo_input = Files.newInputStream(Paths.get(mongo_props))) {
      prop.load(mongo_input);
    }

    String errorBase = "Missing property: ";

    String dbname = prop.getProperty("databaseName");
    if (dbname == null) {
      throw new RuntimeException(errorBase + "databaseName");
    }
    String username = prop.getProperty("databaseUsername");
    if (username == null) {
      throw new RuntimeException(errorBase + "databaseUsername");
    }
    String pw_property = prop.getProperty("databasePassword");
    if (pw_property == null) {
      throw new RuntimeException(errorBase + "databasePassword");
    }

    char[] password = pw_property.toCharArray();

    credential = MongoCredential.createScramSha1Credential(username, dbname, password);

    return credential;
  }

  /**
   * Method to get a MongoDb collection name from property file
   *
   * @return A MongoDb collection name
   */
  public static String getMongoChallengeCollName(String ApplicationRoot, String path) {
    Properties prop = new Properties();

    // Some over paranoid input validation never hurts.
    path = path.replaceAll("\\.", "").replaceAll("/", "");
    log.debug("Path = " + path);

    String mongo_props =
        new File(
                Objects.requireNonNull(
                        Database.class.getResource("/challenges/" + path + ".properties"))
                    .getFile())
            .getAbsolutePath();

    log.debug("Properties File: " + mongo_props);

    try (InputStream mongo_input = Files.newInputStream(Paths.get(mongo_props))) {

      prop.load(mongo_input);

    } catch (IOException e) {
      log.error("Could not load properties file: " + e.toString());
      throw new RuntimeException(e);
    }

    String dbCollectionName = prop.getProperty("databaseCollection");
    if (dbCollectionName == null) {
      throw new RuntimeException("Missing property : databaseCollection");
    }

    return dbCollectionName;
  }

  /**
   * Method to get a MongoDb Connection
   *
   * @return A MongoDb Connection @throws IOException @throws
   */
  public static MongoClient getMongoDbConnection(String ApplicationRoot) {
    Properties prop = new Properties();

    // Mongo DB URL from mongo.properties
    String mongo_props = Constants.MONGO_DB_PROP;

    try (InputStream mongo_input = Files.newInputStream(Paths.get(mongo_props))) {

      prop.load(mongo_input);

    } catch (IOException e) {
      log.error("Could not load properties file: " + e.toString());
      throw new RuntimeException(e);
    }

    String errorBase = "Missing property :";

    String connectionHost = prop.getProperty("connectionHost");
    if (connectionHost == null) {
      throw new RuntimeException(errorBase + "connectionHost");
    }
    String connectionPort = prop.getProperty("connectionPort");
    if (connectionPort == null) {
      throw new RuntimeException(errorBase + "connectionPort");
    }
    String connectTimeout = prop.getProperty("connectTimeout");
    if (connectTimeout == null) {
      throw new RuntimeException(errorBase + "connectTimeout");
    }
    String socketTimeout = prop.getProperty("socketTimeout");
    if (socketTimeout == null) {
      throw new RuntimeException(errorBase + "socketTimeout");
    }

    String serverSelectionTimeout = prop.getProperty("serverSelectionTimeout");
    if (serverSelectionTimeout == null) {
      throw new RuntimeException(errorBase + "serverSelectionTimeout");
    }

    MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
    optionsBuilder.connectTimeout(Integer.parseInt(connectTimeout));
    optionsBuilder.socketTimeout(Integer.parseInt(socketTimeout));
    optionsBuilder.serverSelectionTimeout(Integer.parseInt(serverSelectionTimeout));
    MongoClientOptions mongoOptions = optionsBuilder.build();

    try (MongoClient mongoClient =
        new MongoClient(
            new ServerAddress(connectionHost, Integer.parseInt(connectionPort)), mongoOptions)) {

      log.debug("Mongo Client: " + mongoClient);
      return mongoClient;

    } catch (NumberFormatException e) {
      log.fatal("The port in the properties file is not a number: " + e);
      throw new RuntimeException(e);

    } catch (MongoSocketOpenException e) {
      log.fatal("Mongo Doesn't seem to be running: " + e);
      e.printStackTrace();
      throw new RuntimeException(e);

    } catch (MongoSocketException e) {
      log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
      throw new RuntimeException(e);

    } catch (MongoException e) {
      log.fatal("Something went wrong with Mongo: " + e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Method to get a MongoDb Connection
   *
   * @param credential to connect to the MongoDB
   * @return A MongoDb Connection
   */
  public static MongoClient getMongoDbConnection(
      String ApplicationRoot, MongoCredential credential) {

    Properties prop = new Properties();
    MongoClient mongoClient = null;

    // Mongo DB URL from mongo.properties
    String mongo_props = Constants.MONGO_DB_PROP;

    try (InputStream mongo_input = Files.newInputStream(Paths.get(mongo_props))) {

      prop.load(mongo_input);

    } catch (IOException e) {
      log.error("Could not load properties file: " + e.toString());
      throw new RuntimeException(e);
    }

    String errorBase = "Missing property :";

    String connectionHost = prop.getProperty("connectionHost");
    if (connectionHost == null) {
      throw new RuntimeException(errorBase + "connectionHost");
    }
    String connectionPort = prop.getProperty("connectionPort");
    if (connectionPort == null) {
      throw new RuntimeException(errorBase + "connectionPort");
    }
    String connectTimeout = prop.getProperty("connectTimeout");
    if (connectTimeout == null) {
      throw new RuntimeException(errorBase + "connectTimeout");
    }
    String socketTimeout = prop.getProperty("socketTimeout");
    if (socketTimeout == null) {
      throw new RuntimeException(errorBase + "socketTimeout");
    }

    String serverSelectionTimeout = prop.getProperty("serverSelectionTimeout");
    if (serverSelectionTimeout == null) {
      throw new RuntimeException(errorBase + "serverSelectionTimeout");
    }

    MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
    optionsBuilder.connectTimeout(Integer.parseInt(connectTimeout));
    optionsBuilder.socketTimeout(Integer.parseInt(socketTimeout));
    optionsBuilder.serverSelectionTimeout(Integer.parseInt(serverSelectionTimeout));
    MongoClientOptions mongoOptions = optionsBuilder.build();

    try {
      mongoClient =
          new MongoClient(
              new ServerAddress(connectionHost, Integer.parseInt(connectionPort)),
              credential,
              mongoOptions);

      log.debug("Connection Host: " + connectionHost);
      log.debug("Connection Port: " + Integer.parseInt(connectionPort));
      log.debug("Connection Creds: " + Collections.singletonList(credential));
    } catch (NumberFormatException e) {
      log.fatal("The port in the properties file is not a number: " + e);
      throw new RuntimeException(e);

    } catch (MongoSocketException | MongoTimeoutException e) {
      log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
      throw new RuntimeException(e);

    } catch (MongoException e) {
      log.fatal("Something went wrong with Mongo: " + e);
      e.printStackTrace();
      throw new RuntimeException(e);

    } catch (Exception e) {
      log.fatal("Something went wrong: " + e);
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    log.debug("Mongo Client: " + mongoClient);

    return mongoClient;
  }

  /**
   * Method to get a MongoDb Database
   *
   * @param mongoClient mongoDb connection
   * @return A MongoDb Database
   */
  public static DB getMongoDatabase(MongoClient mongoClient) {
    DB mongoDb = null;

    Properties prop = new Properties();

    // Mongo DB URL from mongo.properties
    String mongo_props = Constants.MONGO_DB_PROP;

    try (InputStream mongo_input = Files.newInputStream(Paths.get(mongo_props))) {

      prop.load(mongo_input);

    } catch (IOException e) {
      log.error("Could not load properties file: " + e.toString());
      throw new RuntimeException(e);
    }

    String dbname = prop.getProperty("databaseName");
    if (dbname == null) {
      throw new RuntimeException("Missing property : databaseName");
    }

    try {
      mongoDb = mongoClient.getDB(dbname);
    } catch (MongoSocketException | MongoTimeoutException e) {
      log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
    } catch (MongoException e) {
      log.fatal("Something went wrong with Mongo: " + e);
      e.printStackTrace();
    } catch (Exception e) {
      log.fatal("Something went wrong: " + e);
      e.printStackTrace();
    }

    return mongoDb;
  }

  /**
   * Method to execute a mongo database JS file in a Mongo Database
   *
   * @param file the file to run
   * @param mongoClient to get connection to db
   */
  public static void executeMongoScript(File file, MongoClient mongoClient) throws IOException {
    String data = FileUtils.readFileToString(file, Charset.defaultCharset());

    DB db = MongoDatabase.getMongoDatabase(mongoClient);

    DBObject script = new BasicDBObject();
    script.put("eval", String.format(data));

    CommandResult result = db.command(script);

    log.debug("Mongo Result: " + result);
  }
}
