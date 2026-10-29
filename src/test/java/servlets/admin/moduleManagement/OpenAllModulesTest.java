package servlets.admin.moduleManagement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import testUtils.LogCapture;

/** OpenAllModules must not write the submitted CSRF token to the log (#972) */
public class OpenAllModulesTest {

  private static final String SUBMITTED_TOKEN = "submittedCsrfToken972";

  @Test
  public void doPost_SubmittedToken_IsNotLogged() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openAllModules");
    request.setCookies(new Cookie("token", "cookieCsrfToken972"));
    request.addParameter("csrfToken", SUBMITTED_TOKEN);
    MockHttpServletResponse response = new MockHttpServletResponse();

    try (LogCapture logs = LogCapture.of(OpenAllModules.class)) {
      new OpenAllModules().doPost(request, response);

      assertFalse(
          logs.messagesAtOrAbove(Level.ALL).stream().anyMatch(m -> m.contains(SUBMITTED_TOKEN)),
          "CSRF token was logged: " + logs.messagesAtOrAbove(Level.ALL));
    }
    assertTrue(response.getContentAsString().contains("loggedOutSheep"));
  }

  @Test
  public void doPost_MissingTokenParameter_RespondsAsLoggedOut() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openAllModules");
    request.setCookies(new Cookie("token", "cookieCsrfToken972"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    new OpenAllModules().doPost(request, response);

    assertTrue(response.getContentAsString().contains("loggedOutSheep"));
  }
}
