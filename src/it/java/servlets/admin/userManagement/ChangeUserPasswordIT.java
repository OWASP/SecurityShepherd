package servlets.admin.userManagement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import dbProcs.Getter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import testUtils.TestProperties;

/**
 * Tests the ChangeUserPassword servlet, in particular that the new password is never written to the
 * application log.
 */
public class ChangeUserPasswordIT {

  private static final Logger log = LogManager.getLogger(ChangeUserPasswordIT.class);
  private static String applicationRoot = new String();
  private static String lang = "en_GB";
  private static final String NEW_PASSWORD = "Zq7!NewPwd-LeakCanary-4821";

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private org.apache.logging.log4j.core.Logger servletLogger;
  private Level originalLevel;
  private CapturingAppender appender;

  /** Collects every message logged to the logger it is attached to. */
  private static class CapturingAppender extends AbstractAppender {
    private final List<String> messages = new CopyOnWriteArrayList<>();

    CapturingAppender() {
      super("ChangeUserPasswordCapture", null, null, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
      messages.add(event.getMessage().getFormattedMessage());
      if (event.getThrown() != null) {
        messages.add(event.getThrown().toString());
      }
    }
  }

  /** Creates DB or Restores DB to Factory Defaults before running tests */
  @BeforeAll
  public static void resetDatabase() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);

    TestProperties.createMysqlResource();

    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();
  }

  @BeforeEach
  public void setUp() {
    TestProperties.setTestPropertiesFileDirectory(log);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();

    servletLogger =
        (org.apache.logging.log4j.core.Logger) LogManager.getLogger(ChangeUserPassword.class);
    originalLevel = servletLogger.getLevel();
    appender = new CapturingAppender();
    appender.start();
    servletLogger.addAppender(appender);
    servletLogger.setLevel(Level.DEBUG);
  }

  @AfterEach
  public void tearDown() {
    servletLogger.removeAppender(appender);
    appender.stop();
    servletLogger.setLevel(originalLevel);
  }

  public String doThePost(String csrfToken, String playerId, String password) throws Exception {
    int expectedResponseCode = 302;
    String moduleClassName = "ChangeUserPassword";
    log.debug("Creating " + moduleClassName + " Servlet Instance");
    ChangeUserPassword servlet = new ChangeUserPassword();
    servlet.init(new MockServletConfig(moduleClassName));

    request.addParameter("csrfToken", csrfToken);
    request.addParameter("player", playerId);
    request.addParameter("password", password);

    log.debug("Running doPost");
    servlet.doPost(request, response);

    if (response.getStatus() != expectedResponseCode) {
      fail(
          moduleClassName
              + " Servlet Returned "
              + response.getStatus()
              + " Code. "
              + expectedResponseCode
              + " Expected");
    }
    return response.getContentAsString();
  }

  @Test
  public void testNewPasswordIsNotLogged() throws Exception {
    String adminName = "configAdminTester";
    TestProperties.verifyTestAdmin(log, applicationRoot, adminName, adminName);
    log.debug("Signing in as Admin User Through LoginServlet");
    TestProperties.loginDoPost(log, request, response, adminName, adminName, null, lang);
    assertNotNull(response.getCookie("token"), "No CSRF Token Was Returned from Login Servlet");
    String csrfToken = response.getCookie("token").getValue();
    assertFalse(csrfToken.isEmpty(), "No CSRF token returned from Login Servlet");
    request.setCookies(response.getCookies());

    String targetName = "passwordResetTarget";
    TestProperties.verifyTestUser(log, applicationRoot, targetName, targetName);
    String targetId = Getter.getUserIdFromName(applicationRoot, targetName);
    assertFalse(targetId == null || targetId.isEmpty(), "Target user was not created");

    String responseBody = doThePost(csrfToken, targetId, NEW_PASSWORD);

    // Guards against the servlet bailing out early, which would make the log assertion vacuous.
    assertTrue(
        responseBody.contains("password has been updated"),
        "Password was not updated. Response: " + responseBody);
    assertTrue(
        appender.messages.size() > 0 && appender.messages.contains("Updating Player Password"),
        "Servlet debug logging was not captured, so the log assertion below proves nothing");

    for (String message : appender.messages) {
      assertFalse(
          message.contains(NEW_PASSWORD), "New password was written to the log: " + message);
    }
  }
}
