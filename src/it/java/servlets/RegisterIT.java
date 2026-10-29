package servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import testUtils.LogCapture;
import testUtils.TestProperties;
import utils.OpenRegistration;

public class RegisterIT {

  private static final Logger log = LogManager.getLogger(RegisterIT.class);
  private static final String SESSION_TOKEN = "sessionCsrfToken971";
  private static final String SUBMITTED_TOKEN = "submittedCsrfToken971";

  @BeforeAll
  public static void openRegistration() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();
    OpenRegistration.enable();
  }

  @AfterAll
  public static void closeRegistration() {
    OpenRegistration.disable();
  }

  /** A CSRF token mismatch must not write either token to the log (#971) */
  @Test
  public void doPost_TokenMismatch_DoesNotLogTokens() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/register");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("csrfToken", SESSION_TOKEN);
    request.setSession(session);
    request.addParameter("csrfToken", SUBMITTED_TOKEN);
    MockHttpServletResponse response = new MockHttpServletResponse();
    Register servlet = new Register();
    servlet.init(new MockServletConfig("Register"));

    try (LogCapture logs = LogCapture.of(Register.class)) {
      servlet.doPost(request, response);

      List<String> messages = logs.messagesAtOrAbove(Level.ALL);
      assertFalse(
          messages.stream().anyMatch(m -> m.contains(SESSION_TOKEN)),
          "Session CSRF token was logged: " + messages);
      assertFalse(
          messages.stream().anyMatch(m -> m.contains(SUBMITTED_TOKEN)),
          "Submitted CSRF token was logged: " + messages);
      assertTrue(
          messages.stream().anyMatch(m -> m.contains("CSRF Tokens did not match")),
          "Mismatch was not logged: " + messages);
    }
  }
}
