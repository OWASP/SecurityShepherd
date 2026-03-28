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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to create MongoDb connections using a singleton pattern for connection reuse. MongoClient
 * already includes internal connection pooling, so we only need one instance. <br>
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

  // Singleton instance for the base MongoClient (no credentials)
  private static volatile MongoClient baseMongoClient;

  // Map of credential-based MongoClients (keyed by credential source/database)
  private static final ConcurrentHashMap<String, MongoClient> credentialClients =
      new ConcurrentHashMap<>();

  // Lock object for thread-safe initialization
  private static final Object lock = new Object();

  // Default pool configuration
  private static final int DEFAULT_CONNECTIONS_PER_HOST = 10;
  private static final int DEFAULT_MIN_CONNECTIONS_PER_HOST = 2;

  /**
   * Method to close a MongoDb connection. Note: With singleton pattern, individual connections
   * should NOT be closed by callers. This method is kept for API compatibility but does nothing for
   * singleton clients. Use shutdown() to close all connections when the application is shutting
   * down.
   *
   * @param conn The connection (ignored for singleton clients)
   * @deprecated Use shutdown() instead when shutting down the application
   */
  @Deprecated
  public static void closeConnection(MongoClient conn) {
    // Do not close singleton connections - they are managed by the pool
    // Only close if it's not one of our managed singletons
    if (conn != null && conn != baseMongoClient && !credentialClients.containsValue(conn)) {
      conn.close();
    } else {
      log.debug("Ignoring close request for singleton MongoClient - use shutdown() instead");
    }
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
   * Method to get a singleton MongoDb Connection. MongoClient has internal connection pooling, so
   * the same instance is reused.
   *
   * @param ApplicationRoot The running context of the application (kept for API compatibility)
   * @return A singleton MongoDb Connection
   */
  public static MongoClient getMongoDbConnection(String ApplicationRoot) {
    if (baseMongoClient == null) {
      synchronized (lock) {
        if (baseMongoClient == null) {
          baseMongoClient = createMongoClient(null);
          log.info("Created singleton MongoClient instance");
        }
      }
    }
    return baseMongoClient;
  }

  /**
   * Creates a MongoClient with the configuration from properties file.
   *
   * @param credential Optional credential for authenticated connections
   * @return A new MongoClient instance
   */
  private static MongoClient createMongoClient(MongoCredential credential) {
    Properties prop = loadMongoProperties();

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

    // Configure connection pool options
    MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
    optionsBuilder.connectTimeout(Integer.parseInt(connectTimeout));
    optionsBuilder.socketTimeout(Integer.parseInt(socketTimeout));
    optionsBuilder.serverSelectionTimeout(Integer.parseInt(serverSelectionTimeout));

    // Connection pool settings
    int connectionsPerHost =
        getIntProperty(prop, "pool.connectionsPerHost", DEFAULT_CONNECTIONS_PER_HOST);
    int minConnectionsPerHost =
        getIntProperty(prop, "pool.minConnectionsPerHost", DEFAULT_MIN_CONNECTIONS_PER_HOST);
    optionsBuilder.connectionsPerHost(connectionsPerHost);
    optionsBuilder.minConnectionsPerHost(minConnectionsPerHost);

    MongoClientOptions mongoOptions = optionsBuilder.build();
    ServerAddress serverAddress =
        new ServerAddress(connectionHost, Integer.parseInt(connectionPort));

    try {
      MongoClient mongoClient;
      if (credential != null) {
        mongoClient = new MongoClient(serverAddress, Arrays.asList(credential), mongoOptions);
        log.debug("Created MongoClient with credentials for: " + credential.getSource());
      } else {
        mongoClient = new MongoClient(serverAddress, mongoOptions);
        log.debug("Created MongoClient without credentials");
      }
      log.debug("Connection Host: " + connectionHost + ", Port: " + connectionPort);
      return mongoClient;

    } catch (NumberFormatException e) {
      log.fatal("The port in the properties file is not a number: " + e);
      throw new RuntimeException(e);

    } catch (MongoSocketOpenException e) {
      log.fatal("Mongo Doesn't seem to be running: " + e);
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
   * Loads MongoDB properties from the configuration file.
   *
   * @return Properties object containing MongoDB configuration
   */
  private static Properties loadMongoProperties() {
    Properties prop = new Properties();
    String mongo_props = Constants.MONGO_DB_PROP;

    try (InputStream mongo_input = new FileInputStream(mongo_props)) {
      prop.load(mongo_input);
    } catch (IOException e) {
      log.error("Could not load properties file: " + e.toString());
      throw new RuntimeException(e);
    }

    return prop;
  }

  /** Helper method to get an integer property with a default value. */
  private static int getIntProperty(Properties prop, String key, int defaultValue) {
    String value = prop.getProperty(key);
    if (value != null) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        log.warn(
            "Invalid integer value for property '{}': {}, using default: {}",
            key,
            value,
            defaultValue);
      }
    }
    return defaultValue;
  }

  /**
   * Method to get a singleton MongoDb Connection with credentials. Each unique credential gets its
   * own MongoClient instance (with internal pooling).
   *
   * @param ApplicationRoot The running context of the application (kept for API compatibility)
   * @param credential The credential to connect to MongoDB
   * @return A singleton MongoDb Connection for the given credential
   */
  public static MongoClient getMongoDbConnection(
      String ApplicationRoot, MongoCredential credential) {

    if (credential == null) {
      return getMongoDbConnection(ApplicationRoot);
    }

    // Use the credential source (database name) as the key
    String credentialKey = credential.getSource() + ":" + credential.getUserName();

    return credentialClients.computeIfAbsent(
        credentialKey,
        key -> {
          log.debug("Creating new MongoClient for credential: " + credentialKey);
          return createMongoClient(credential);
        });
  }

  /**
   * Method to get a MongoDb Database
   *
   * @param mongoClient mongoDb connection
   * @return A MongoDb Database
   */
  public static DB getMongoDatabase(MongoClient mongoClient) {
    DB mongoDb = null;

    Properties prop = loadMongoProperties();

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

  /**
   * Shuts down all MongoDB connections. This should be called when the application is shutting
   * down.
   */
  public static void shutdown() {
    synchronized (lock) {
      log.info("Shutting down MongoDB connections...");

      // Close base client
      if (baseMongoClient != null) {
        baseMongoClient.close();
        baseMongoClient = null;
        log.debug("Base MongoClient closed");
      }

      // Close all credential-based clients
      for (MongoClient client : credentialClients.values()) {
        client.close();
      }
      credentialClients.clear();

      log.info("All MongoDB connections shut down successfully");
    }
  }

  /** Resets the singleton instances. This is primarily for testing purposes. */
  public static void resetInstance() {
    shutdown();
  }

  /**
   * Checks if the base MongoClient has been initialized.
   *
   * @return true if initialized, false otherwise
   */
  public static boolean isInitialized() {
    return baseMongoClient != null;
  }
}
