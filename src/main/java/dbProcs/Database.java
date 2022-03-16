package dbProcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to create database connections using the FileInputProperties.readfile method to gather
 * property information Initiated by Getter.java, Setter.java <br>
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
 * @author Mark
 */
public class Database {

  private static final Logger log = LogManager.getLogger(Database.class);

  /**
   * This method is used by the application to open an connection to the database
   *
   * @param conn The connection to close
   * @throws SQLException
   */
  public static Connection getConnection(
      String driverType,
      String connectionURL,
      String dbOptions,
      String dbUsername,
      String dbPassword)
      throws SQLException {

    try {
      Class.forName(driverType).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    if (dbOptions.length() > 0) {
      connectionURL += "?" + dbOptions;
    }

    Connection conn = DriverManager.getConnection(connectionURL, dbUsername, dbPassword);

    return conn;
  }

  /**
   * This method is used by the application to close an open connection to a database server
   *
   * @param conn The connection to close
   */
  public static void closeConnection(Connection conn) {

    // log.debug("Closing database connection");
    try {
      conn.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method is used by the application to get a connection to the secure database sever based
   * on the input path to a specific properties file.
   *
   * @param ApplicationRoot The running context of the application.
   * @param path The path to the properties file to use for this connection. this is filtered for
   *     path traversal attacks
   * @return A connection to the secure database server
   * @throws IOException
   * @throws FileNotFoundException
   * @throws SQLException
   */
  public static Connection getChallengeConnection(String ApplicationRoot, String path)
      throws SQLException {
    // Some over paranoid input validation never hurts.
    path = path.replaceAll("\\.", "").replaceAll("/", "");
    log.debug("Path = " + path);

    Connection conn = null;
    Properties prop = new Properties();

    // Pull Driver and DB URL out of database.properties

    String mysql_props = Constants.MYSQL_DB_PROP;

    try (InputStream mysql_input = new FileInputStream(mysql_props)) {

      prop.load(mysql_input);

    } catch (IOException e) {
      log.error("Could not load properties file: " + e.toString());
      throw new RuntimeException(e);
    }

    String errorBase = "Missing property :";

    String connectionURL = prop.getProperty("databaseConnectionURL");
    if (connectionURL == null) {
      String message = errorBase + "connectionURL";
      log.fatal(message);
      throw new RuntimeException(message);
    }

    if (connectionURL.contains("?")) {
      // Check for old-style challenge properties files that put options in connection url
      String message = "Connection URL cannot contain options!";
      log.fatal(message);
      throw new RuntimeException(message);
    }

    String driverType = prop.getProperty("DriverType");
    if (driverType == null) {
      String message = errorBase + "DriverType";
      log.fatal(message);
      throw new RuntimeException(message);
    }

    // Pull DB Schema, Schema User name and Schema Password from level specific
    // properties File

    String challenge_props =
        new File(Database.class.getResource("/challenges/" + path + ".properties").getFile())
            .getAbsolutePath();

    log.debug("Level Properties File = " + path + ".properties");

    try (InputStream mysql_input = new FileInputStream(challenge_props)) {

      prop.load(mysql_input);

    } catch (IOException e) {
      log.error("Could not load properties file: " + e.toString());
      throw new RuntimeException(e);
    }
    String challenge_connectionURL = prop.getProperty("databaseConnectionURL");
    if (challenge_connectionURL == null) {
      throw new RuntimeException(errorBase + "connectionURL");
    }
    String dbOptions = prop.getProperty("databaseOptions");
    if (dbOptions == null) {
      log.debug(
          "Did not find database options, defaulting to"
              + " useUnicode=true&character_set_server=utf8mb4");

      dbOptions = "useUnicode=true&character_set_server=utf8mb4";
    }
    String username = prop.getProperty("databaseUsername");
    if (username == null) {
      throw new RuntimeException(errorBase + "databaseUsername");
    }
    String password = prop.getProperty("databasePassword");
    if (password == null) {
      throw new RuntimeException(errorBase + "databasePassword");
    }

    connectionURL += challenge_connectionURL;

    conn = getConnection(driverType, connectionURL, dbOptions, username, password);

    return conn;
  }

  public static Connection getCoreConnection() throws SQLException, IOException {
    Connection conn = getCoreConnection("");

    return conn;
  }

  /**
   * @param ApplicationRoot @return Connection to core schema with admin privileges @throws
   *     FileNotFoundException @throws SQLException @throws
   * @throws IOException Returns connection to core schema in database
   * @throws FileNotFoundException
   * @throws SQLException
   * @throws RuntimeException
   */
  public static Connection getCoreConnection(String ApplicationRoot) throws SQLException {
    Connection conn = null;
    Properties prop = new Properties();

    // Pull Driver and DB URL out of database.properties

    String mysql_props = Constants.MYSQL_DB_PROP;

    try (InputStream mysql_input = new FileInputStream(mysql_props)) {

      prop.load(mysql_input);

    } catch (IOException e) {
      log.error("Could not load properties file: " + e.toString());
      throw new RuntimeException(e);
    }

    String errorBase = "Missing property :";

    String connectionURL = prop.getProperty("databaseConnectionURL");
    if (connectionURL == null) {
      throw new RuntimeException(errorBase + "connectionURL");
    }
    String databaseSchema = prop.getProperty("databaseSchema");
    if (databaseSchema == null) {
      throw new RuntimeException(errorBase + "databaseSchema");
    }
    String dbOptions = prop.getProperty("databaseOptions");
    if (dbOptions == null) {
      throw new RuntimeException(errorBase + "databaseOptions");
    }
    String driverType = prop.getProperty("DriverType");
    if (driverType == null) {
      throw new RuntimeException(errorBase + "DriverType");
    }
    String username = prop.getProperty("databaseUsername");
    if (username == null) {
      throw new RuntimeException(errorBase + "databaseUsername");
    }
    String password = prop.getProperty("databasePassword");
    if (password == null) {
      throw new RuntimeException(errorBase + "databasePassword");
    }

    connectionURL += databaseSchema;

    conn = getConnection(driverType, connectionURL, dbOptions, username, password);

    return conn;
  }

  public static Connection getDatabaseConnection(String ApplicationRoot)
      throws SQLException, IOException {
    return getDatabaseConnection(ApplicationRoot, false);
  }

  /**
   * This method is used by the application to get a connection to the secure database sever
   *
   * @param ApplicationRoot The running context of the application.
   * @return A connection to the secure database server
   * @throws SQLException
   * @throws IOException
   * @throws RuntimeException
   */
  public static Connection getDatabaseConnection(String ApplicationRoot, boolean allowMulti)
      throws SQLException, IOException {
    Connection conn = null;

    Properties prop = new Properties();

    // Pull Driver and DB URL out of database.properties

    String mysql_props = Constants.MYSQL_DB_PROP;

    try (InputStream mysql_input = new FileInputStream(mysql_props)) {

      prop.load(mysql_input);
    }

    String errorBase = "Missing property :";

    String connectionURL = prop.getProperty("databaseConnectionURL");
    if (connectionURL == null) {
      throw new RuntimeException(errorBase + "connectionURL");
    }
    String dbOptions = prop.getProperty("databaseOptions");
    if (dbOptions == null) {
      throw new RuntimeException(errorBase + "databaseOptions");
    }
    String driverType = prop.getProperty("DriverType");
    if (driverType == null) {
      throw new RuntimeException(errorBase + "DriverType");
    }
    String username = prop.getProperty("databaseUsername");
    if (username == null) {
      throw new RuntimeException(errorBase + "databaseUsername");
    }
    String password = prop.getProperty("databasePassword");
    if (password == null) {
      throw new RuntimeException(errorBase + "databasePassword");
    }

    if (allowMulti) {
      if (dbOptions.length() > 0) {
        dbOptions += "&";
      }
      dbOptions += "allowMultiQueries=true";
    }

    conn = getConnection(driverType, connectionURL, dbOptions, username, password);

    return conn;
  }

  /**
   * This method is used by the application to get a connection to the secure database sever's SQL
   * injection Lesson schema
   *
   * @param ApplicationRoot The running context of the application.
   * @return A connection to the secure database server
   * @throws SQLException
   * @throws IOException
   */
  public static Connection getSqlInjLessonConnection(String ApplicationRoot)
      throws SQLException, IOException {
    Connection conn = null;

    Properties prop = new Properties();

    String mysql_props = Constants.MYSQL_DB_PROP;

    try (InputStream mysql_input = new FileInputStream(mysql_props)) {

      prop.load(mysql_input);
    }

    String errorBase = "Missing property :";

    String connectionURL = prop.getProperty("databaseConnectionURL");
    if (connectionURL == null) {
      throw new RuntimeException(errorBase + "connectionURL");
    }
    String dbOptions = prop.getProperty("databaseOptions");
    if (dbOptions == null) {
      throw new RuntimeException(errorBase + "databaseOptions");
    }
    String driverType = prop.getProperty("DriverType");
    if (driverType == null) {
      throw new RuntimeException(errorBase + "DriverType");
    }

    // Pull Schema, User name and Password from SqlInjLesson.properties
    String sql_inj_props = ApplicationRoot + "/WEB-INF/classes/lessons/SqlInjLesson.properties";

    try (InputStream sql_inj_input = new FileInputStream(sql_inj_props)) {

      prop.load(sql_inj_input);
    }

    String injection_URL = prop.getProperty("databaseConnectionURL");
    if (injection_URL == null) {
      throw new RuntimeException(errorBase + "SQL injection connectionURL");
    }
    String username = prop.getProperty("databaseUsername");
    if (username == null) {
      throw new RuntimeException(errorBase + "SQL injection databaseUsername");
    }
    String password = prop.getProperty("databasePassword");
    if (password == null) {
      throw new RuntimeException(errorBase + "SQL injection databasePassword");
    }

    conn = getConnection(driverType, connectionURL + injection_URL, dbOptions, username, password);

    return conn;
  }
}
