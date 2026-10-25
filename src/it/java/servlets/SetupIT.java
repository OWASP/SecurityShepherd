package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dbProcs.ConnectionPool;
import dbProcs.Constants;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
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
    Setup.resetInstalledCache();
    Setup.isInstalled();
  }

  private void removeDatabaseProps() {
    FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
  }

  /** Configures the test database and checks the app now counts as installed. */
  private void installApp() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();
    ConnectionPool.initialize();
    assertTrue(ConnectionPool.isInitialized(), "the connection pool didn't start");
    Setup.resetInstalledCache();
    assertTrue(Setup.isInstalled(), "the app should count as installed");
  }

  /** Leaves the app uninstalled: no database.properties, so setup.jsp would show its form. */
  private void uninstallApp() {
    TestProperties.setTestPropertiesFileDirectory(log);
    new File(Constants.CATALINA_CONF).mkdirs();
    removeDatabaseProps();
    Setup.resetInstalledCache();
    assertFalse(Setup.isInstalled(), "the app shouldn't count as installed");
  }

  /**
   * POSTs the setup form with every field setup.jsp sends. It doesn't override or upgrade the
   * database, so a POST that gets through only writes database.properties.
   */
  private MockHttpServletResponse postSetup(
      String dbHost, String dbPort, String dbUser, String dbPass, String dbAuth)
      throws ServletException, IOException {
    Setup servlet = new Setup();
    servlet.init(new MockServletConfig("Setup"));
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/setup");
    request.setServletPath("/setup");
    request.addParameter("dbhost", dbHost);
    request.addParameter("dbport", dbPort);
    request.addParameter("dbuser", dbUser);
    request.addParameter("dbpass", dbPass);
    request.addParameter("dboverride", "false");
    request.addParameter("dbauth", dbAuth);
    request.addParameter("mhost", "localhost");
    request.addParameter("mport", "27017");
    request.addParameter("enableMongoChallenge", "");
    request.addParameter("unsafeLevels", "");
    MockHttpServletResponse response = new MockHttpServletResponse();
    servlet.doPost(request, response);
    return response;
  }

  /** POSTs the setup form with the test database's real connection details. */
  private MockHttpServletResponse postSetupWithTestDatabase(String dbAuth)
      throws ServletException, IOException {
    Dotenv dotenv = Dotenv.load();
    return postSetup(
        dotenv.get("TEST_MYSQL_HOST"),
        dotenv.get("TEST_MYSQL_PORT"),
        "root",
        dotenv.get("TEST_MYSQL_PASSWORD"),
        dbAuth);
  }

  private static void assertSetupRefused(MockHttpServletResponse response)
      throws UnsupportedEncodingException {
    String body = response.getContentAsString();
    assertFalse(body.contains("Database Configuration Complete"), "setup went ahead: " + body);
  }

  private static void assertAuthenticationFailed(MockHttpServletResponse response)
      throws UnsupportedEncodingException {
    assertSetupRefused(response);
    String body = response.getContentAsString();
    assertTrue(body.contains("Authentication failure"), "setup didn't refuse the token: " + body);
    assertFalse(
        new File(Constants.MYSQL_DB_PROP).exists(), "setup wrote database.properties anyway");
  }

  /** #1043: a missing token file must not let an empty token through. */
  @Test
  public void doPost_notInstalled_missingAuthFile_emptyDbauth_isRefused() throws Exception {
    uninstallApp();
    Files.deleteIfExists(Paths.get(Constants.SETUP_AUTH));

    assertAuthenticationFailed(postSetupWithTestDatabase(""));
  }

  /** #1043: an empty token file must not let an empty token through. */
  @Test
  public void doPost_notInstalled_emptyAuthFile_emptyDbauth_isRefused() throws Exception {
    uninstallApp();
    Files.write(Paths.get(Constants.SETUP_AUTH), new byte[0]);

    assertAuthenticationFailed(postSetupWithTestDatabase(""));
    String token =
        FileUtils.readFileToString(new File(Constants.SETUP_AUTH), StandardCharsets.UTF_8);
    assertFalse(token.isBlank(), "setup didn't replace the empty token file");
  }

  /** The first-run path: the token from SecurityShepherd.auth still works. */
  @Test
  public void doPost_notInstalled_validToken_configuresDatabase() throws Exception {
    uninstallApp();
    String token =
        FileUtils.readFileToString(new File(Constants.SETUP_AUTH), StandardCharsets.UTF_8);
    assertFalse(token.isBlank(), "isInstalled() didn't generate a token");

    MockHttpServletResponse response = postSetupWithTestDatabase(token);

    String body = response.getContentAsString();
    assertTrue(body.contains("Database Configuration Complete"), "setup failed: " + body);
    assertTrue(
        new File(Constants.MYSQL_DB_PROP).exists(), "setup didn't write database.properties");
    assertFalse(
        new File(Constants.SETUP_AUTH).exists(), "setup didn't remove the token file afterwards");
  }

  /** #1043: once installed, setup is refused without a token. */
  @Test
  public void doPost_installed_emptyDbauth_isForbidden() throws Exception {
    installApp();
    try {
      Files.deleteIfExists(Paths.get(Constants.SETUP_AUTH));

      MockHttpServletResponse response = postSetup("", "", "", "", "");

      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
      assertSetupRefused(response);
    } finally {
      ConnectionPool.shutdown();
    }
  }

  /** #1043: once installed, setup is refused even with a valid token. */
  @Test
  public void doPost_installed_validToken_isForbidden() throws Exception {
    installApp();
    try {
      String token = "1043-" + UUID.randomUUID();
      Files.write(Paths.get(Constants.SETUP_AUTH), token.getBytes(StandardCharsets.UTF_8));

      MockHttpServletResponse response = postSetup("", "", "", "", token);

      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
      assertSetupRefused(response);
    } finally {
      Files.deleteIfExists(Paths.get(Constants.SETUP_AUTH));
      ConnectionPool.shutdown();
    }
  }

  /** #1043: once installed, SetupFilter doesn't pass /setup on to the servlet. */
  @Test
  public void setupFilter_installed_forbidsSetupServlet() throws Exception {
    installApp();
    try {
      MockHttpServletRequest request = new MockHttpServletRequest("POST", "/setup");
      request.setServletPath("/setup");
      MockHttpServletResponse response = new MockHttpServletResponse();
      MockFilterChain chain = new MockFilterChain();

      new SetupFilter().doFilter(request, response, chain);

      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
      assertNull(chain.getRequest(), "SetupFilter passed /setup on to the servlet");
    } finally {
      ConnectionPool.shutdown();
    }
  }

  /** Once installed, SetupFilter still passes other pages on, setup.jsp included. */
  @Test
  public void setupFilter_installed_passesOtherPages() throws Exception {
    installApp();
    try {
      for (String path : new String[] {"/login.jsp", "/setup.jsp"}) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        MockFilterChain chain = new MockFilterChain();

        new SetupFilter().doFilter(request, new MockHttpServletResponse(), chain);

        assertNotNull(chain.getRequest(), "SetupFilter didn't pass " + path + " on");
      }
    } finally {
      ConnectionPool.shutdown();
    }
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
