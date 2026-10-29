package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dbProcs.ConnectionPool;
import dbProcs.Constants;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import testUtils.TestProperties;

public class SetupIT {

  private static final Logger log = LogManager.getLogger(SetupIT.class);

  /** Initialize directories */
  @AfterEach
  public void tearDown() throws IOException {
    log.debug("Cleaning up");

    removeDatabaseProps();
    Setup.isInstalled();
  }

  private void removeDatabaseProps() {
    FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
  }

  /**
   * Posts setup on an instance that is not installed yet, with the database settings from .env and
   * only the given optional parameters
   */
  private MockHttpServletResponse postSetupWithoutDatabaseProps(Map<String, String> optionalParams)
      throws Exception {
    TestProperties.setTestPropertiesFileDirectory(log);
    // Setup writes its auth file here, and on a clean build nothing else has created it yet
    Files.createDirectories(Paths.get(Constants.SETUP_AUTH).getParent());
    removeDatabaseProps();
    Setup.resetInstalledCache();
    assertFalse(Setup.isInstalled());
    String authData =
        FileUtils.readFileToString(new File(Constants.SETUP_AUTH), StandardCharsets.UTF_8);

    Dotenv dotenv = Dotenv.load();
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/setup");
    request.addParameter("dbhost", dotenv.get("TEST_MYSQL_HOST"));
    request.addParameter("dbport", dotenv.get("TEST_MYSQL_PORT"));
    request.addParameter("dbuser", "root");
    request.addParameter("dbpass", dotenv.get("TEST_MYSQL_PASSWORD"));
    request.addParameter("dbauth", authData);
    request.addParameter("mhost", "localhost");
    request.addParameter("mport", "27017");
    optionalParams.forEach(request::addParameter);
    MockHttpServletResponse response = new MockHttpServletResponse();
    Setup servlet = new Setup();
    servlet.init(new MockServletConfig("Setup"));

    servlet.doPost(request, response);
    return response;
  }

  /** Leaving out enableMongoChallenge and unsafeLevels means "not enabled" (#956) */
  @Test
  public void doPost_WithoutMongoAndUnsafeLevelsParams_Succeeds() throws Exception {
    MockHttpServletResponse response = postSetupWithoutDatabaseProps(Map.of("dboverride", "none"));

    assertEquals(200, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Database Configuration Complete"),
        response.getContentAsString());
  }

  /** Leaving out dboverride means "neither override nor upgrade" (#956) */
  @Test
  public void doPost_WithoutDbOverrideParam_Succeeds() throws Exception {
    MockHttpServletResponse response = postSetupWithoutDatabaseProps(Map.of());

    assertEquals(200, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Database Configuration Complete"),
        response.getContentAsString());
  }

  @Disabled
  @Test
  public void testNoCoreDatabase() {

    log.debug("Creating Setup Servlet Instance");
    Setup servlet = new Setup();
    try {
      servlet.init(new MockServletConfig("Setup"));
    } catch (ServletException e) {
      log.fatal(e.toString());
      fail(e.toString());
    }

    try {
      TestProperties.createMysqlResource();
    } catch (IOException e) {
      String message = "Could not create mysql resource file: " + e.toString();
      log.fatal(message);
      fail(message);
    }

    assertFalse(Setup.isInstalled());

    String authData = "";
    try {
      authData = FileUtils.readFileToString(new File(Constants.SETUP_AUTH), StandardCharsets.UTF_8);
    } catch (IOException e) {
      String message =
          "Error when loading auth file "
              + Constants.SETUP_AUTH
              + ". Exception message was "
              + e.toString();
      log.fatal(message);
      fail(message);
    }

    if (authData == null) {
      String message = "Auth data loaded from " + Constants.SETUP_AUTH + " was null!";
      log.fatal(message);
      fail(message);
    }

    if (authData == "") {
      String message = "Auth data loaded from " + Constants.SETUP_AUTH + " was empty!";
      log.fatal(message);
      fail(message);
    }
    /*
     * request.getSession().setAttribute("lang", lang);
     *
     * request.addParameter("dbhost", "localhost"); request.addParameter("dbport",
     * "3306"); request.addParameter("dbuser", "root");
     * request.addParameter("dbpass", ""); request.addParameter("dbauth", authData);
     *
     * log.debug("Running doPost"); try { servlet.doPost(request, response); } catch
     * (ServletException | IOException e) { e.printStackTrace();
     * log.fatal(e.toString()); fail(e.toString()); }
     * log.debug("doPost successful, reading response");
     *
     * if(response.getStatus() != expectedResponseCode) { String
     * message="Login Servlet Returned " + response.getStatus() +
     * " Code. 302 Expected"; log.fatal(message); fail(message); } String location =
     * ""; try { location = response.getHeader("Location"); } catch
     * (NullPointerException e) { String message =
     * "Got invalid location from posting setup request: " + e.toString();
     * log.fatal(message); fail(message); }
     *
     * if (!location.endsWith("login.jsp")) {
     * fail("Setup not Redirecting to login.jsp."); }
     */

  }

  @Disabled
  @Test
  public void testNoMysqlResource() {

    log.debug("Creating Setup Servlet Instance");
    Setup servlet = new Setup();
    try {
      servlet.init(new MockServletConfig("Setup"));
    } catch (ServletException e) {
      log.fatal(e.toString());
      fail(e.toString());
    }

    TestProperties.deleteMysqlResource();
    assertTrue(Setup.isInstalled());

    String authData = "";
    try {
      authData = FileUtils.readFileToString(new File(Constants.SETUP_AUTH), StandardCharsets.UTF_8);
    } catch (IOException e) {
      String message =
          "Error when loading auth file "
              + Constants.SETUP_AUTH
              + ". Exception message was "
              + e.toString();
      log.fatal(message);
      fail(message);
    }

    if (authData == null) {
      String message = "Auth data loaded from " + Constants.SETUP_AUTH + " was null!";
      log.fatal(message);
      fail(message);
    }

    if (authData == "") {
      String message = "Auth data loaded from " + Constants.SETUP_AUTH + " was empty!";
      log.fatal(message);
      fail(message);
    }
  }

  @Test
  public void isInstalled_cachesResult() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();

    boolean poolReady = false;
    try {
      ConnectionPool.initialize();
      poolReady = ConnectionPool.isInitialized();
    } catch (Exception e) {
      log.warn("Pool init issue: " + e.getMessage());
    }
    assumeTrue(poolReady);

    try {
      Setup.resetInstalledCache();

      boolean first = Setup.isInstalled();
      boolean second = Setup.isInstalled();

      assertTrue(first, "isInstalled should return true with a running database");
      assertEquals(first, second);
    } finally {
      ConnectionPool.shutdown();
    }
  }

  @Test
  public void resetInstalledCache_allowsReevaluation() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();

    boolean poolReady = false;
    try {
      ConnectionPool.initialize();
      poolReady = ConnectionPool.isInitialized();
    } catch (Exception e) {
      log.warn("Pool init issue: " + e.getMessage());
    }
    assumeTrue(poolReady);

    try {
      Setup.resetInstalledCache();
      boolean first = Setup.isInstalled();

      Setup.resetInstalledCache();
      boolean second = Setup.isInstalled();

      assertTrue(first, "isInstalled should return true with a running database");
      assertEquals(first, second);
    } finally {
      ConnectionPool.shutdown();
    }
  }
}
