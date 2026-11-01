package servlets.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import dbProcs.GetterIT;
import dbProcs.Setter;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import testUtils.LogCapture;
import testUtils.TestProperties;
import utils.CountdownHandler;
import utils.ModuleBlock;
import utils.Validate;

public class GetModuleIT {

  private static String lang = "en_GB";
  private static final Logger log = LogManager.getLogger(GetModuleIT.class);
  private static String applicationRoot = new String();
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  /** Creates DB or Restores DB to Factory Defaults before running tests */
  @BeforeAll
  public static void resetDatabase() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);

    TestProperties.createMysqlResource();

    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();
  }

  @BeforeEach
  public void setup() {
    log.debug("Setting Up Blank Request and Response");
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    // Open All modules
    if (!Setter.openAllModules(applicationRoot, false)) {
      fail("Could not Mark All Modules As Open");
    }
    // Ensure no Module block enabled
    ModuleBlock.reset();
    if (ModuleBlock.blockerEnabled) {
      String message = "Unable to disable module block";
      log.fatal(message);
      fail(message);
    }
  }

  /**
   * Method to Simulate the interaction with the getModule servlet.
   *
   * @param moduleId The ID of the Module to Search For
   * @param csrfToken The CSRF Token of the User
   * @return The Content of the Response (Which is supposed to be the location of the module)
   * @throws ServletException
   * @throws IOException
   * @throws Exception
   */
  public String getModuleDoPost(String moduleId, String csrfToken)
      throws ServletException, IOException {

    int expectedResponseCode = 302;

    log.debug("Creating GetModule Servlet Instance");
    GetModule servlet = new GetModule();
    servlet.init(new MockServletConfig("GetModule"));

    // Setup Servlet Parameters and Attributes
    log.debug("Setting Up Params and Atrributes");
    request.addParameter("moduleId", moduleId);
    // Adding Correct CSRF Token (Token Submitted)
    request.addParameter("csrfToken", csrfToken);

    log.debug("Running doPost");
    servlet.doPost(request, response);

    if (response.getStatus() != expectedResponseCode) {
      fail(
          "GetModule Servlet Returned "
              + response.getStatus()
              + " Code. "
              + expectedResponseCode
              + " Expected");
    } else {
      log.debug("302 OK Detected");
      log.debug(
          "Servlet Successful, returning location retrieved: " + response.getContentAsString());
      return (response.getContentAsString());
    }

    return null;
  }

  /**
   * This test checks the module address returned when the requested module is currently blocked
   *
   * @throws SQLException
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testGetBlockedModule() throws SQLException, ServletException, IOException {
    String moduleId = new String("20e755179a5840be5503d42bb3711716235005ea"); // CSRF 1
    String userName = "getModule5";

    // Verify User Exists in DB
    GetterIT.verifyTestUser(applicationRoot, userName, userName);
    // Sign in as Normal User
    log.debug("Signing in as User Through LoginServlet");
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    log.debug("Login Servlet Complete, Getting CSRF Token");
    if (response.getCookie("token") == null) {
      fail("No CSRF Token Was Returned from Login Servlet");
    }
    String csrfToken = response.getCookie("token").getValue();
    if (csrfToken.isEmpty()) {
      String message = new String("No CSRF token returned from Login Servlet");
      log.fatal(message);
      fail(message);
    } else {
      // Blocking Module
      ModuleBlock.blockerId = moduleId;
      ModuleBlock.blockerEnabled = true;
      // Add Cookies from Response to outgoing request
      request.setCookies(response.getCookies());
      String moduleAddress = getModuleDoPost(moduleId, csrfToken);
      log.debug(moduleAddress);
      // Resetting Module Block
      ModuleBlock.reset();
      if (moduleAddress.equalsIgnoreCase("../blockedMessage.jsp")) {
        log.debug("Blocked Module Address Returned: PASS");
      } else {
        String message = "Module Address Returned was not the Blocked Message Page";
        log.fatal(message + ". Should be ../blockedMessage.jsp");
        log.debug("Returned: " + moduleAddress);
        fail(message);
      }
    }
  }

  /**
   * This test retreives the location of a challenge module
   *
   * @throws SQLException
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testGetChallenge() throws SQLException, ServletException, IOException {
    String moduleId = new String("20e755179a5840be5503d42bb3711716235005ea"); // CSRF 1
    String levelHash =
        new String("s74a796e84e25b854906d88f622170c1c06817e72b526b3d1e9a6085f429cf52");
    String userName = "getModule2";

    // Verify User Exists in DB
    GetterIT.verifyTestUser(applicationRoot, userName, userName);
    // Sign in as Normal User
    log.debug("Signing in as User Through LoginServlet");
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    log.debug("Login Servlet Complete, Getting CSRF Token");
    if (response.getCookie("token") == null) {
      fail("No CSRF Token Was Returned from Login Servlet");
    }
    String csrfToken = response.getCookie("token").getValue();
    if (csrfToken.isEmpty()) {
      String message = new String("No CSRF token returned from Login Servlet");
      log.fatal(message);
      fail(message);
    } else {
      // Add Cookies from Response to outgoing request
      request.setCookies(response.getCookies());
      String moduleAddress = getModuleDoPost(moduleId, csrfToken);
      if (moduleAddress.endsWith("challenges/" + levelHash + ".jsp")) {
        log.debug("Correct Location Returned");
      } else {
        String message = "The Incorrect Location was Returned for the CSRF 1 Challenge.";
        log.fatal(message);
        log.debug("location returned: " + moduleAddress);
        log.debug("Should be        : challenges&#x2f;" + levelHash + ".jsp");
        fail(message);
      }
    }
  }

  @Test
  public void testGetModule() throws SQLException, ServletException, IOException {
    String moduleId =
        new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); // Insecure Direct Object References
    // Module Id
    String levelHash =
        new String("fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100");
    String userName = "getModule1";

    // Verify User Exists in DB
    GetterIT.verifyTestUser(applicationRoot, userName, userName);
    // Sign in as Normal User
    log.debug("Signing in as User Through LoginServlet");
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    log.debug("Login Servlet Complete, Getting CSRF Token");
    if (response.getCookie("token") == null) {
      fail("No CSRF Token Was Returned from Login Servlet");
    }
    String csrfToken = response.getCookie("token").getValue();
    if (csrfToken.isEmpty()) {
      String message = new String("No CSRF token returned from Login Servlet");
      log.fatal(message);
      fail(message);
    } else {
      // Add Cookies from Response to outgoing request
      request.setCookies(response.getCookies());
      String moduleAddress = getModuleDoPost(moduleId, csrfToken);
      if (moduleAddress.endsWith("lessons/" + levelHash + ".jsp")) {
        log.debug("Correct Location Returned");
      } else {
        String message =
            "The Incorrect Location was Returned for the Insecure Director Object Reference"
                + " Lesson.";
        log.fatal(message);
        log.debug("location returned: " + moduleAddress);
        fail(message);
      }
    }
  }

  /**
   * Signs in through the Login servlet and returns the CSRF token, with the session cookies added
   * to the outgoing request.
   */
  private String signIn(String userName) {
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    if (response.getCookie("token") == null || response.getCookie("token").getValue().isEmpty()) {
      fail("No CSRF Token Was Returned from Login Servlet");
    }
    request.setCookies(response.getCookies());
    return response.getCookie("token").getValue();
  }

  /**
   * Opening a module is something every player does, so it must not raise the unauthorised admin
   * access alert (#735)
   */
  @Test
  public void testGetModuleAsPlayerLogsNoAdminAlert()
      throws SQLException, ServletException, IOException {
    String moduleId = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; // Insecure Direct Object Ref
    String levelHash = "fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100";
    String userName = "getModule6";
    GetterIT.verifyTestUser(applicationRoot, userName, userName);
    String csrfToken = signIn(userName);

    try (LogCapture validateLogs = LogCapture.of(Validate.class)) {
      String moduleAddress = getModuleDoPost(moduleId, csrfToken);
      assertTrue(
          moduleAddress.endsWith("lessons/" + levelHash + ".jsp"),
          "Unexpected module address: " + moduleAddress);
      assertEquals(List.of(), validateLogs.messagesAtOrAbove(Level.WARN));
    }
  }

  /** Admins can still open modules while the CTF isn't running, and players still can't */
  @Test
  public void testGetModuleWhenCtfEnded() throws SQLException, ServletException, IOException {
    String moduleId = "0dbea4cb5811fff0527184f99bd5034ca9286f11"; // Insecure Direct Object Ref
    String levelHash = "fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100";
    String adminName = "getModuleAdmin1";
    String userName = "getModule7";
    GetterIT.verifyTestAdmin(applicationRoot, adminName, adminName);
    GetterIT.verifyTestUser(applicationRoot, userName, userName);

    CountdownHandler.disableStartTime();
    CountdownHandler.disableLockTime();
    CountdownHandler.setEndTime(LocalDateTime.now().minusDays(1));
    try {
      String adminToken = signIn(adminName);
      String adminAddress = getModuleDoPost(moduleId, adminToken);
      assertTrue(
          adminAddress.endsWith("lessons/" + levelHash + ".jsp"),
          "Admin should get the module while the CTF has ended, got: " + adminAddress);

      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();
      String userToken = signIn(userName);
      assertEquals("../blockedMessage.jsp", getModuleDoPost(moduleId, userToken));
    } finally {
      CountdownHandler.disableEndTime();
    }
  }

  /**
   * This test submits a valid Module Id but with an invalid CSRF Token Pair
   *
   * @throws SQLException
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testGetModuleBadCsrfToken() throws SQLException, ServletException, IOException {
    String moduleId = new String("20e755179a5840be5503d42bb3711716235005ea"); // CSRF 1
    String userName = "getModule5";

    // Verify User Exists in DB
    GetterIT.verifyTestUser(applicationRoot, userName, userName);
    // Sign in as Normal User
    log.debug("Signing in as User Through LoginServlet");
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    log.debug("Login Servlet Complete, Getting CSRF Token");
    if (response.getCookie("token") == null) {
      fail("No CSRF Token Was Returned from Login Servlet");
    }
    String csrfToken = response.getCookie("token").getValue();
    if (csrfToken.isEmpty()) {
      String message = new String("No CSRF token returned from Login Servlet");
      log.fatal(message);
      fail(message);
    } else {
      // Add Cookies from Response to outgoing request
      request.setCookies(response.getCookies());
      String moduleAddress = getModuleDoPost(moduleId, "TheWrongCsrfToken");
      if (moduleAddress.isEmpty()) {
        log.debug("No Module Address Returned: PASS");
      } else {
        String message = "Module Address Returned When Bad ID Was Submitted";
        log.fatal(message + ". Nothing should be returned");
        log.debug("Returned: " + moduleAddress);
        fail(message);
      }
    }
  }

  /**
   * This test attempts to retrieve a module with a non existant identifier
   *
   * @throws SQLException
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testGetModuleBadId() throws SQLException, ServletException, IOException {
    String moduleId = new String("ThisModuleDoesNotExist");
    String userName = "getModule3";

    // Verify User Exists in DB
    GetterIT.verifyTestUser(applicationRoot, userName, userName);
    // Sign in as Normal User
    log.debug("Signing in as User Through LoginServlet");
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    log.debug("Login Servlet Complete, Getting CSRF Token");
    if (response.getCookie("token") == null) {
      fail("No CSRF Token Was Returned from Login Servlet");
    }
    String csrfToken = response.getCookie("token").getValue();
    if (csrfToken.isEmpty()) {
      String message = new String("No CSRF token returned from Login Servlet");
      log.fatal(message);
      fail(message);
    } else {
      // Add Cookies from Response to outgoing request
      request.setCookies(response.getCookies());
      String moduleAddress = getModuleDoPost(moduleId, csrfToken);
      if (moduleAddress.isEmpty()) {
        log.debug("No Module Address Returned: PASS");
      } else {
        String message = "Module Address Returned When Bad ID Was Submitted";
        log.fatal(message + ". Nothing should be returned");
        log.debug("Returned: " + moduleAddress);
      }
    }
  }

  /**
   * This test submits a null value to the getModule Servlet
   *
   * @throws IOException
   * @throws ServletException
   * @throws SQLException
   */
  @Test
  public void testGetModuleNullId() throws ServletException, IOException, SQLException {
    String moduleId = null;
    String userName = "getModule4";

    // Verify User Exists in DB
    GetterIT.verifyTestUser(applicationRoot, userName, userName);
    // Sign in as Normal User
    log.debug("Signing in as User Through LoginServlet");
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    log.debug("Login Servlet Complete, Getting CSRF Token");
    if (response.getCookie("token") == null) {
      fail("No CSRF Token Was Returned from Login Servlet");
    }
    String csrfToken = response.getCookie("token").getValue();
    if (csrfToken.isEmpty()) {
      String message = new String("No CSRF token returned from Login Servlet");
      log.fatal(message);
      fail(message);
    } else {
      // Add Cookies from Response to outgoing request
      request.setCookies(response.getCookies());
      String moduleAddress = getModuleDoPost(moduleId, csrfToken);
      if (moduleAddress.isEmpty()) {
        log.debug("No Module Address Returned: PASS");
      } else {
        String message = "Module Address Returned When Bad ID Was Submitted";
        log.fatal(message + ". Nothing should be returned");
        log.debug("Returned: " + moduleAddress);
      }
    }
  }

  @Test
  public void testGetModuleWhenClosed() throws SQLException, ServletException, IOException {
    String moduleId =
        new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); // Insecure Direct Object References
    // Module Id
    String levelHash =
        new String("fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100");
    String userName = "getModule1";

    // Verify User Exists in DB
    GetterIT.verifyTestUser(applicationRoot, userName, userName);
    // Sign in as Normal User
    log.debug("Signing in as User Through LoginServlet");
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    log.debug("Login Servlet Complete, Getting CSRF Token");
    if (response.getCookie("token") == null) {
      fail("No CSRF Token Was Returned from Login Servlet");
    }
    String csrfToken = response.getCookie("token").getValue();
    if (csrfToken.isEmpty()) {
      String message = new String("No CSRF token returned from Login Servlet");
      log.fatal(message);
      fail(message);
    } else {
      if (!Setter.closeAllModules(applicationRoot)) {
        fail("Could not Mark All Modules As Closed");
      }
      // Add Cookies from Response to outgoing request
      request.setCookies(response.getCookies());
      String moduleAddress = getModuleDoPost(moduleId, csrfToken);
      if (moduleAddress.endsWith("lessons&#x2f;" + levelHash + ".jsp")) {
        String message =
            "Insecure Director Object Reference Lesson Address Returned when module was closed";
        log.fatal(message);
        log.debug("location returned: " + moduleAddress);
        fail(message);
      } else {
        log.debug("Address not returned! Pass!");
      }
    }
  }
}
